package dinhgiang.dev.hungthinh.services.implement;

import dinhgiang.dev.hungthinh.exceptions.UserMessageException;
import dinhgiang.dev.hungthinh.models.dtos.tableElectricTiers.TableElectricTierCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.tableElectricTiers.TableElectricTierGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.tableElectricTiers.TableElectricTierUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.bases.TableElectricTier;
import dinhgiang.dev.hungthinh.repositories.TableElectricTierRepository;
import dinhgiang.dev.hungthinh.services.interfaces.ITableElectricTierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Lớp cài đặt cho ITableElectricTierService.
 * Quản lý các bậc thang giá điện.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Service
@RequiredArgsConstructor
public class TableElectricTierService implements ITableElectricTierService {
    private final TableElectricTierRepository tableElectricTierRepository;

    public List<TableElectricTierGetResponse> getAllTableElectricTiers() {
        List<TableElectricTier> tableElectricTiers = tableElectricTierRepository.findAll();
        return tableElectricTiers.stream().map(
                tableElectricTier -> TableElectricTierGetResponse.builder()
                        .id(tableElectricTier.getId())
                        .tierOrder(tableElectricTier.getTierOrder())
                        .limitValue(tableElectricTier.getLimitValue())
                        .unitPrice(tableElectricTier.getUnitPrice())
                        .build()
        ).toList();
    }


    public Long createTableElectricTier(TableElectricTierCreateRequest apiRequest) {
        TableElectricTier tableElectricTier = new TableElectricTier();
        tableElectricTier.setTierOrder(apiRequest.getTierOrder());
        tableElectricTier.setLimitValue(apiRequest.getLimitValue());
        tableElectricTier.setUnitPrice(apiRequest.getUnitPrice());
        tableElectricTierRepository.save(tableElectricTier);
        return tableElectricTier.getId();
    }

    public Long updateTableElectricTier(Long tableElectricTierId, TableElectricTierUpdateRequest apiRequest) {
        TableElectricTier tableElectricTier = tableElectricTierRepository.findById(tableElectricTierId)
                .orElseThrow(() -> new UserMessageException("Bảng điện lũy tiến không tồn tại"));
        if (apiRequest.getTierOrder() != null) {
            tableElectricTier.setTierOrder(apiRequest.getTierOrder());
        }
        if (apiRequest.getLimitValue() != null) {
            tableElectricTier.setLimitValue(apiRequest.getLimitValue());
        }
        if (apiRequest.getUnitPrice() != null) {
            tableElectricTier.setUnitPrice(apiRequest.getUnitPrice());
        }
        tableElectricTierRepository.save(tableElectricTier);
        return tableElectricTier.getId();
    }

    public Void deleteTableElectricTier(Long tableElectricTierId) {
        tableElectricTierRepository.deleteById(tableElectricTierId);
        return null;
    }

    /**
     * Hàm tính tiền điện nội suy theo ngày
     * @param totalKwh: Tổng số chữ điện tiêu thụ (vd: 300)
     * @param startDate: Ngày đầu kỳ (vd: 2026-04-16)
     * @param endDate: Ngày cuối kỳ (vd: 2026-05-16)
     */
    public BigDecimal calculatorElectricFeeWithTier(
            Long totalKwh,
            LocalDate startDate,
            LocalDate endDate,
            Integer numberOfHouseholds
    ) {
        List<TableElectricTier> tiers = tableElectricTierRepository.findAll();
       //Tính số ngày sử dụng thực tế
        long actualDay = ChronoUnit.DAYS.between(startDate, endDate);
        if (actualDay <= 0) actualDay = 30;

        //Hệ số nội suy
        double ratio = (double) actualDay / 30.0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        double remainingKwh = totalKwh;

        //Duyệt qua từng bậc để áp dụng định mức mới
        for (TableElectricTier electricTier : tiers) {
            if (remainingKwh <= 0) break;
            double adjustedLimit;
            if (electricTier.getTierOrder() == 6) {
                adjustedLimit = remainingKwh;
            } else {
                adjustedLimit = Math.round(electricTier.getLimitValue() * ratio * numberOfHouseholds);
            }

            //Số kWh tiêu thụ trong bậc này
            double kwhThisTier = Math.min(remainingKwh, adjustedLimit);

            //Tính tiền bậc này
            BigDecimal tierCost = BigDecimal.valueOf(kwhThisTier).multiply(electricTier.getUnitPrice());
            totalAmount = totalAmount.add(tierCost);

            //Trừ đi số kWh đã tính để sang bậc tiếp theo
            remainingKwh = remainingKwh - kwhThisTier;
        }
        return totalAmount.setScale(0, RoundingMode.HALF_UP);
    }
}
