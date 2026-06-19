package dinhgiang.dev.hungthinh.services.implement;

import dinhgiang.dev.hungthinh.exceptions.UserMessageException;
import dinhgiang.dev.hungthinh.models.dtos.evns.EvnBillResponse;
import dinhgiang.dev.hungthinh.models.entities.bases.Resident;
import dinhgiang.dev.hungthinh.repositories.ResidentRepository;
import dinhgiang.dev.hungthinh.services.interfaces.IEvnMockService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.Random;

/**
 * Lớp cài đặt (Implementation) cho IEvnMockService.
 * Giả lập gọi API EVN để lấy hóa đơn điện.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Service
@RequiredArgsConstructor
public class EvnMockService implements IEvnMockService {
    private final ResidentRepository residentRepository;

    public EvnBillResponse getBillByResidentId(Long residentId) {
        return getBillByResidentId(residentId, true);
    }

    public EvnBillResponse getBillByResidentId(Long residentId, boolean simulateDelay) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new UserMessageException("Khách hàng không tồn tại"));
        if (simulateDelay) {
            try {
                Thread.sleep(800);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        //Trong thực tế chỗ này sẽ query db EVN
        //Ở đây ta mock dữ liệu ngẫu nhiên hoặc có thể gọi TableElectricTierResponsitory
        Random random = new Random();
        int kwh = 125 + random.nextInt(500);
        String responseName = resident.getFullName() + ' ' + random.nextInt(1000000);
        String responseAddress = "Chung cư HungThinh " + "Phòng số: " + resident.getApartment().getApartmentNumber() + " Tầng: " + resident.getApartment().getFloor() + " Tòa: " + resident.getApartment().getBlock();
        return EvnBillResponse.builder()
                .customerName(responseName)
                .address(responseAddress)
                .billingPeriod(YearMonth.now().minusMonths(1))
                .kwhConsumed(kwh)
                .status("UNPAID")
                .build();
    }
}
