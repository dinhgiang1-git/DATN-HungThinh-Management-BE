package dinhgiang.dev.hungthinh.services.implement;

import dinhgiang.dev.hungthinh.exceptions.UserMessageException;
import dinhgiang.dev.hungthinh.models.dtos.water.WaterBillResponse;
import dinhgiang.dev.hungthinh.models.entities.bases.Resident;
import dinhgiang.dev.hungthinh.repositories.ResidentRepository;
import dinhgiang.dev.hungthinh.services.interfaces.IWaterMockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.Random;

/**
 * Lớp cài đặt cho IWaterMockService.
 * Giả lập gọi API lấy hóa đơn nước.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Service
@RequiredArgsConstructor
public class WaterMockService implements IWaterMockService {
    private final ResidentRepository residentRepository;

    public WaterBillResponse getBillByResidentId(Long residentId) {
        return getBillByResidentId(residentId, true);
    }

    public WaterBillResponse getBillByResidentId(Long residentId, boolean simulateDelay) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new UserMessageException("Khách hàng không tồn tại"));
        if (simulateDelay) {
            try {
                Thread.sleep(600);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        //Trong thực tế chỗ này sẽ query db công ty nước
        //Ở đây ta mock dữ liệu ngẫu nhiên
        Random random = new Random();
        int cubicMeter = 5 + random.nextInt(30); // 5-35 m³
        String responseName = resident.getFullName() + ' ' + random.nextInt(1000000);
        String responseAddress = "Chung cư HungThinh " + "Phòng số: " + resident.getApartment().getApartmentNumber() + " Tầng: " + resident.getApartment().getFloor() + " Tòa: " + resident.getApartment().getBlock();
        return WaterBillResponse.builder()
                .customerName(responseName)
                .address(responseAddress)
                .billingPeriod(YearMonth.now().minusMonths(1))
                .cubicMeterConsumed(cubicMeter)
                .status("UNPAID")
                .build();
    }
}
