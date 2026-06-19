package dinhgiang.dev.hungthinh.controllers;

import dinhgiang.dev.hungthinh.models.dtos.payments.ManualPaymentRequest;
import dinhgiang.dev.hungthinh.models.entities.global.ApiResponse;
import dinhgiang.dev.hungthinh.services.interfaces.IPaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/payment")
@RestController
@RequiredArgsConstructor
public class PaymentController extends BaseController{
    private final IPaymentService paymentService;

    @GetMapping("/momo/{invoiceId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<String>> createPayment(
            @PathVariable Long invoiceId,
            @RequestParam(value = "source", defaultValue = "admin") String source,
            @RequestParam(value = "returnUrl", required = false) String returnUrl
    ) {
       return success(paymentService.createPayment(invoiceId, source, returnUrl), "Tạo thanh toán thành công");
    }

    @GetMapping("/vnpay/{invoiceId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<String>> createVnPayPayment(
            @PathVariable Long invoiceId,
            @RequestParam(value = "source", defaultValue = "admin") String source,
            @RequestParam(value = "returnUrl", required = false) String returnUrl,
            HttpServletRequest request
    ) {
        return success(
                paymentService.createVnPayPayment(invoiceId, source, returnUrl, resolveClientIp(request)),
                "Tạo thanh toán VNPay thành công"
        );
    }

    @GetMapping("/momo-callback")
    public ResponseEntity<ApiResponse<Void>> momoReturn(HttpServletRequest request) {
        return success(paymentService.momoReturn(request), "Thanh toán thành công");
    }

    @GetMapping("/vnpay-callback")
    public ResponseEntity<ApiResponse<Void>> vnPayReturn(HttpServletRequest request) {
        return success(paymentService.vnPayReturn(request), "Thanh toán VNPay thành công");
    }

    @GetMapping("/vnpay-ipn")
    public ResponseEntity<java.util.Map<String, String>> vnPayIpn(HttpServletRequest request) {
        return ResponseEntity.ok(paymentService.vnPayIpn(request));
    }

    @PostMapping("/momo-ipn")
    public ResponseEntity<ApiResponse<Void>> momoIpn(
            HttpServletRequest request,
            @RequestBody(required = false) java.util.Map<String, Object> body
    ) {
        return success(paymentService.momoIpn(request, body), "IPN received");
    }

    @PostMapping("/manual")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> createManualPayment(@Valid @RequestBody ManualPaymentRequest request) {
        return success(paymentService.createManualPayment(request), "Ghi nhận thanh toán thành công");
    }

    @PostMapping("/cash-request")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<Long>> requestCashPayment(@Valid @RequestBody ManualPaymentRequest request) {
        return success(paymentService.requestCashPayment(request), "Tạo đề xuất thanh toán tiền mặt thành công");
    }

    @PatchMapping("/cash-request/{paymentId}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> confirmCashPayment(@PathVariable Long paymentId) {
        return success(paymentService.confirmCashPayment(paymentId), "Xác nhận thanh toán tiền mặt thành công");
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}
