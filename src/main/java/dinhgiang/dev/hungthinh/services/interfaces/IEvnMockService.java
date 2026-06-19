package dinhgiang.dev.hungthinh.services.interfaces;

import dinhgiang.dev.hungthinh.models.dtos.evns.EvnBillResponse;

/**
 * Interface định nghĩa dịch vụ gọi API giả lập của EVN (Tập đoàn Điện lực VN).
 * Dùng để lấy thông tin hóa đơn điện định kỳ của cư dân.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface IEvnMockService {

    /**
     * Lấy thông tin hóa đơn điện tử EVN dựa trên mã khách hàng (ở đây dùng residentId).
     *
     * @param residentId ID của cư dân
     * @return EvnBillResponse chứa thông tin chỉ số điện và số tiền
     */
    EvnBillResponse getBillByResidentId(Long residentId);
}
