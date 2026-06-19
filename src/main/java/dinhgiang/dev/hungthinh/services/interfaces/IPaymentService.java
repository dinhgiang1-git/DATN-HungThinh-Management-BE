package dinhgiang.dev.hungthinh.services.interfaces;

import dinhgiang.dev.hungthinh.models.dtos.payments.ManualPaymentRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Interface định nghĩa các nghiệp vụ liên quan đến Thanh toán hóa đơn.
 * Hỗ trợ tạo giao dịch qua VNPay/MoMo, xử lý callback và xác nhận thanh toán tiền mặt.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface IPaymentService {

    /**
     * Khởi tạo giao dịch thanh toán qua cổng điện tử (VNPAY/MOMO).
     * Sinh ra URL để điều hướng người dùng tới trang thanh toán.
     *
     * @param invoiceId ID của hóa đơn cần thanh toán
     * @param source    Nguồn giao diện thanh toán (admin, resident)
     * @param returnUrl URL MoMo chuyển người dùng về sau khi thanh toán
     * @return URL trang thanh toán
     */
    String createPayment(Long invoiceId, String source, String returnUrl);

    /**
     * Khởi tạo giao dịch thanh toán qua VNPay.
     *
     * @param invoiceId ID của hóa đơn cần thanh toán
     * @param source    Nguồn giao diện thanh toán (admin, resident)
     * @param returnUrl URL VNPay chuyển người dùng về sau khi thanh toán
     * @param clientIp  IP người dùng gửi sang VNPay
     * @return URL trang thanh toán VNPay
     */
    String createVnPayPayment(Long invoiceId, String source, String returnUrl, String clientIp);

    /**
     * Xử lý kết quả trả về từ cổng thanh toán MoMo sau khi giao dịch hoàn tất.
     *
     * @param request HttpServletRequest chứa các tham số từ MoMo (mã giao dịch, trạng thái, ...)
     * @return Void
     */
    Void momoReturn(HttpServletRequest request);

    /**
     * Xử lý IPN MoMo server-to-server. MoMo thường gửi JSON body, khác với redirect query.
     *
     * @param request HttpServletRequest chứa query/form params nếu có
     * @param body    JSON body từ MoMo IPN nếu có
     * @return Void
     */
    Void momoIpn(HttpServletRequest request, Map<String, Object> body);

    /**
     * Xử lý kết quả trả về từ VNPay.
     *
     * @param request HttpServletRequest chứa các tham số vnp_*
     * @return Void
     */
    Void vnPayReturn(HttpServletRequest request);

    /**
     * Xử lý IPN VNPay server-to-server và trả đúng định dạng VNPay yêu cầu.
     *
     * @param request HttpServletRequest chứa các tham số vnp_*
     * @return Map gồm RspCode và Message
     */
    Map<String, String> vnPayIpn(HttpServletRequest request);

    /**
     * Tạo giao dịch thanh toán thủ công (chuyển khoản tay).
     *
     * @param request dữ liệu thanh toán thủ công
     * @return ID của giao dịch payment
     */
    Long createManualPayment(ManualPaymentRequest request);

    /**
     * Yêu cầu thanh toán tiền mặt (thường do Resident gửi yêu cầu).
     *
     * @param request dữ liệu yêu cầu thanh toán
     * @return ID của giao dịch payment (với trạng thái PENDING)
     */
    Long requestCashPayment(ManualPaymentRequest request);

    /**
     * Xác nhận đã nhận được tiền mặt (do Admin/Kế toán thực hiện).
     * Chuyển trạng thái giao dịch sang SUCCESS và cập nhật trạng thái Hóa đơn.
     *
     * @param paymentId ID của giao dịch payment cần xác nhận
     * @return ID của giao dịch payment vừa được xác nhận
     */
    Long confirmCashPayment(Long paymentId);
}
