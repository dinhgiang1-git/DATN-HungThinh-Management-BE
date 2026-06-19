package dinhgiang.dev.hungthinh.services.interfaces;

import dinhgiang.dev.hungthinh.models.dtos.water.WaterBillResponse;

/**
 * Interface định nghĩa dịch vụ gọi API giả lập của Công ty Cấp nước.
 * Dùng để lấy thông tin hóa đơn nước định kỳ của cư dân.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface IWaterMockService {

    /**
     * Lấy thông tin hóa đơn nước dựa trên mã khách hàng (ở đây dùng residentId).
     *
     * @param residentId ID của cư dân
     * @return WaterBillResponse chứa thông tin chỉ số nước và số tiền
     */
    WaterBillResponse getBillByResidentId(Long residentId);
}
