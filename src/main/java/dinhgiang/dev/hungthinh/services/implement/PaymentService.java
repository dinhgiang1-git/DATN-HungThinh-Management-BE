package dinhgiang.dev.hungthinh.services.implement;


import dinhgiang.dev.hungthinh.exceptions.UserMessageException;
import dinhgiang.dev.hungthinh.models.entities.bases.Invoice;
import dinhgiang.dev.hungthinh.models.entities.bases.Payment;
import dinhgiang.dev.hungthinh.models.entities.bases.Resident;
import dinhgiang.dev.hungthinh.models.entities.bases.User;
import dinhgiang.dev.hungthinh.models.entities.enums.InvoiceStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.PaymentMethod;
import dinhgiang.dev.hungthinh.models.entities.enums.PaymentStatus;
import dinhgiang.dev.hungthinh.repositories.InvoiceRepository;
import dinhgiang.dev.hungthinh.repositories.PaymentRepository;
import dinhgiang.dev.hungthinh.repositories.ResidentRepository;
import dinhgiang.dev.hungthinh.repositories.UserRepository;
import dinhgiang.dev.hungthinh.services.interfaces.IPaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import dinhgiang.dev.hungthinh.components.Auditable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Lớp cài đặt (Implementation) cho IPaymentService.
 * Xử lý thanh toán qua cổng điện tử (VNPay, MoMo) và thanh toán tiền mặt.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {
    private final InvoiceRepository invoiceRepository;
    private final MoMoService moMoService;
    private final VNPayService vnPayService;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final ResidentRepository residentRepository;
    private final AccessControlService accessControlService;

    private String[] resolveCurrentPayer() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                String username = userDetails.getUsername();
                Optional<User> user = userRepository.findByUsername(username);
                if (user.isPresent()) {
                    return new String[]{user.get().getFullName(), username};
                }
                Optional<Resident> resident = residentRepository.findByUsername(username);
                if (resident.isPresent()) {
                    return new String[]{resident.get().getFullName(), username};
                }
                return new String[]{username, username};
            }
        } catch (Exception ignored) {}
        return new String[]{null, null};
    }

    @Auditable(action = "CREATE", entityType = "PAYMENT")
    public String createPayment(Long invoiceId, String source, String returnUrl) {
        Invoice invoice = getPayableInvoice(invoiceId);
        String orderInfo = buildOrderInfo(invoice);

        // Encode payer username into extraData for callback
        String[] payer = resolveCurrentPayer();
        String extraData = "";
        if (payer[1] != null) {
            extraData = Base64.getEncoder().encodeToString(payer[1].getBytes());
        }

        return moMoService.createPaymentURL(invoiceId, invoice.getTotalAmount(), source, orderInfo, extraData, returnUrl);
    }

    @Auditable(action = "CREATE", entityType = "PAYMENT")
    @Transactional
    public String createVnPayPayment(Long invoiceId, String source, String returnUrl, String clientIp) {
        Invoice invoice = getPayableInvoice(invoiceId);
        String txnRef = invoiceId + "_" + System.currentTimeMillis();
        String[] payer = resolveCurrentPayer();

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(invoice.getTotalAmount());
        payment.setPaymentDateTime(LocalDateTime.now());
        payment.setPaymentMethod(PaymentMethod.VNPAY);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setTxnRef(txnRef);
        payment.setResponseCode("PENDING");
        payment.setPayerName(payer[0]);
        payment.setPayerUsername(payer[1]);
        paymentRepository.save(payment);

        return vnPayService.createPaymentURL(
                invoiceId,
                invoice.getTotalAmount(),
                source,
                buildOrderInfo(invoice),
                txnRef,
                returnUrl,
                clientIp
        );
    }

    @Transactional
    public Void momoReturn(HttpServletRequest request) {
        return processMomoCallback(extractParams(request));
    }

    @Transactional
    public Void momoIpn(HttpServletRequest request, Map<String, Object> body) {
        Map<String, String> params = extractParams(request);
        if (params.isEmpty() && body != null) {
            body.forEach((key, value) -> {
                if (key != null && value != null) {
                    params.put(key, String.valueOf(value));
                }
            });
        }
        return processMomoCallback(params);
    }

    @Transactional
    public Void vnPayReturn(HttpServletRequest request) {
        return processVnPayCallback(extractParams(request));
    }

    @Transactional
    public Map<String, String> vnPayIpn(HttpServletRequest request) {
        try {
            processVnPayCallback(extractParams(request));
            return vnPayIpnResponse("00", "Confirm Success");
        } catch (UserMessageException e) {
            return vnPayIpnErrorResponse(e.getMessage());
        } catch (Exception e) {
            return vnPayIpnResponse("99", "Unknown error");
        }
    }

    private Map<String, String> extractParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, value) -> {
            if (value != null && value.length > 0) {
                params.put(key, value[0]);
            }
        });
        return params;
    }

    private Void processMomoCallback(Map<String, String> params) {
        String resultCode = params.get("resultCode");
        String orderId = params.get("orderId");
        String transId = params.get("transId");

        if (!moMoService.isValidCallbackSignature(params)) {
            throw new UserMessageException("Chữ ký MoMo không hợp lệ");
        }
        if (!moMoService.isExpectedPartner(params.get("partnerCode"))) {
            throw new UserMessageException("Partner MoMo không hợp lệ");
        }
        requireParam(resultCode, "resultCode");
        requireParam(orderId, "orderId");
        requireParam(params.get("amount"), "amount");

        // orderId format: invoiceId_timestamp
        Long invoiceId = extractInvoiceId(orderId);
        long callbackAmount = parseAmount(params.get("amount"));

        Optional<Payment> processedPayment = paymentRepository.findByPaymentMethodAndTxnRef(PaymentMethod.MOMO, orderId);
        if (processedPayment.isPresent()) {
            return null;
        }

        Invoice invoice = invoiceRepository.findByIdForUpdate(invoiceId)
                .orElseThrow(() -> new UserMessageException("Hóa đơn không tồn tại"));

        processedPayment = paymentRepository.findByPaymentMethodAndTxnRef(PaymentMethod.MOMO, orderId);
        if (processedPayment.isPresent()) {
            return null;
        }

        long expectedAmount = invoice.getTotalAmount().longValue();
        if (callbackAmount != expectedAmount) {
            throw new UserMessageException("Số tiền thanh toán MoMo không khớp hóa đơn");
        }

        if (transId != null && !transId.isBlank() && !"0".equals(transId)) {
            Optional<Payment> existingTransaction = paymentRepository.findByPaymentMethodAndTransactionNo(PaymentMethod.MOMO, transId);
            if (existingTransaction.isPresent()) {
                if (orderId.equals(existingTransaction.get().getTxnRef())) {
                    return null;
                }
                throw new UserMessageException("Mã giao dịch MoMo đã được ghi nhận cho đơn hàng khác");
            }
        }

        if ("0".equals(resultCode) && invoice.getInvoiceStatus() == InvoiceStatus.PAID) {
            throw new UserMessageException("Hóa đơn này đã được thanh toán");
        }

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(BigDecimal.valueOf(callbackAmount));
        payment.setPaymentDateTime(LocalDateTime.now());
        payment.setTxnRef(orderId);
        if (transId != null && !transId.isBlank() && !"0".equals(transId)) {
            payment.setTransactionNo(transId);
        }
        payment.setResponseCode(resultCode);
        payment.setBankCode(params.get("payType"));
        payment.setPaymentMethod(PaymentMethod.MOMO);

        // Resolve payer from extraData
        String extraData = params.get("extraData");
        if (extraData != null && !extraData.isEmpty()) {
            try {
                String payerUsername = new String(Base64.getDecoder().decode(extraData));
                payment.setPayerUsername(payerUsername);
                // Look up full name
                Optional<User> user = userRepository.findByUsername(payerUsername);
                if (user.isPresent()) {
                    payment.setPayerName(user.get().getFullName());
                } else {
                    Optional<Resident> resident = residentRepository.findByUsername(payerUsername);
                    payment.setPayerName(resident.map(Resident::getFullName).orElse(payerUsername));
                }
            } catch (Exception ignored) {}
        }

        if ("0".equals(resultCode)) {
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            invoice.setInvoiceStatus(InvoiceStatus.PAID);
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
        }

        paymentRepository.save(payment);
        invoiceRepository.save(invoice);

        return null;
    }

    private Void processVnPayCallback(Map<String, String> params) {
        if (!vnPayService.isValidCallbackSignature(params)) {
            throw new UserMessageException("Chữ ký VNPay không hợp lệ");
        }

        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionStatus = params.get("vnp_TransactionStatus");
        String amount = params.get("vnp_Amount");
        requireParam(txnRef, "vnp_TxnRef");
        requireParam(responseCode, "vnp_ResponseCode");
        requireParam(amount, "vnp_Amount");

        Long invoiceId = extractInvoiceId(txnRef);
        long callbackAmount = parseVnPayAmount(amount);

        Invoice invoice = invoiceRepository.findByIdForUpdate(invoiceId)
                .orElseThrow(() -> new UserMessageException("Hóa đơn không tồn tại"));

        long expectedAmount = invoice.getTotalAmount().longValue();
        if (callbackAmount != expectedAmount) {
            throw new UserMessageException("Số tiền thanh toán VNPay không khớp hóa đơn");
        }

        Optional<Payment> existingPayment = paymentRepository.findByPaymentMethodAndTxnRef(PaymentMethod.VNPAY, txnRef);
        if (existingPayment.isPresent() && existingPayment.get().getPaymentStatus() == PaymentStatus.SUCCESS) {
            return null;
        }

        String transactionNo = params.get("vnp_TransactionNo");
        if (transactionNo != null && !transactionNo.isBlank() && !"0".equals(transactionNo)) {
            Optional<Payment> existingTransaction = paymentRepository.findByPaymentMethodAndTransactionNo(PaymentMethod.VNPAY, transactionNo);
            if (existingTransaction.isPresent()
                    && (existingPayment.isEmpty() || !existingTransaction.get().getId().equals(existingPayment.get().getId()))) {
                throw new UserMessageException("Mã giao dịch VNPay đã được ghi nhận cho đơn hàng khác");
            }
        }

        boolean success = "00".equals(responseCode) && ("00".equals(transactionStatus) || transactionStatus == null);
        if (success && invoice.getInvoiceStatus() == InvoiceStatus.PAID) {
            throw new UserMessageException("Hóa đơn này đã được thanh toán");
        }

        Payment payment = existingPayment.orElseGet(Payment::new);
        payment.setInvoice(invoice);
        payment.setAmount(BigDecimal.valueOf(callbackAmount));
        payment.setPaymentDateTime(LocalDateTime.now());
        payment.setTxnRef(txnRef);
        if (transactionNo != null && !transactionNo.isBlank() && !"0".equals(transactionNo)) {
            payment.setTransactionNo(transactionNo);
        }
        payment.setResponseCode(responseCode);
        payment.setBankCode(params.get("vnp_BankCode"));
        payment.setPayDate(params.get("vnp_PayDate"));
        payment.setPaymentMethod(PaymentMethod.VNPAY);

        if (success) {
            payment.setPaymentStatus(PaymentStatus.SUCCESS);
            invoice.setInvoiceStatus(InvoiceStatus.PAID);
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
        }

        paymentRepository.save(payment);
        invoiceRepository.save(invoice);

        return null;
    }

    private void requireParam(String value, String paramName) {
        if (value == null || value.isBlank()) {
            throw new UserMessageException("Thiếu tham số thanh toán: " + paramName);
        }
    }

    private Map<String, String> vnPayIpnErrorResponse(String message) {
        if ("Chữ ký VNPay không hợp lệ".equals(message)) {
            return vnPayIpnResponse("97", "Invalid Checksum");
        }
        if ("Hóa đơn không tồn tại".equals(message)) {
            return vnPayIpnResponse("01", "Order not found");
        }
        if ("Hóa đơn này đã được thanh toán".equals(message)) {
            return vnPayIpnResponse("02", "Order already confirmed");
        }
        if ("Số tiền thanh toán VNPay không khớp hóa đơn".equals(message)
                || "Số tiền VNPay không hợp lệ".equals(message)) {
            return vnPayIpnResponse("04", "Invalid amount");
        }
        return vnPayIpnResponse("99", "Unknown error");
    }

    private Map<String, String> vnPayIpnResponse(String rspCode, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("RspCode", rspCode);
        response.put("Message", message);
        return response;
    }

    private Long extractInvoiceId(String orderId) {
        String[] parts = orderId.split("_", 2);
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw new UserMessageException("Mã đơn hàng MoMo không hợp lệ");
        }
        try {
            return Long.parseLong(parts[0]);
        } catch (NumberFormatException e) {
            throw new UserMessageException("Mã hóa đơn MoMo không hợp lệ");
        }
    }

    private long parseAmount(String amount) {
        try {
            long parsedAmount = Long.parseLong(amount);
            if (parsedAmount <= 0) {
                throw new NumberFormatException("amount must be positive");
            }
            return parsedAmount;
        } catch (NumberFormatException e) {
            throw new UserMessageException("Số tiền MoMo không hợp lệ");
        }
    }

    private long parseVnPayAmount(String amount) {
        try {
            long parsedAmount = Long.parseLong(amount);
            if (parsedAmount <= 0 || parsedAmount % 100 != 0) {
                throw new NumberFormatException("amount must be positive and multiplied by 100");
            }
            return parsedAmount / 100;
        } catch (NumberFormatException e) {
            throw new UserMessageException("Số tiền VNPay không hợp lệ");
        }
    }

    private Invoice getPayableInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new UserMessageException("Hóa đơn không tồn tại"));
        accessControlService.assertResidentCanAccessApartment(
                invoice.getApartment(),
                "Bạn không có quyền thanh toán hóa đơn này"
        );

        if (invoice.getInvoiceStatus() == InvoiceStatus.PAID) {
            throw new UserMessageException("Hóa đơn này đã được thanh toán");
        }
        return invoice;
    }

    private String buildOrderInfo(Invoice invoice) {
        String invoiceNumber = invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "HD-" + invoice.getId();
        return "Thanh toan " + invoiceNumber;
    }

    @Auditable(action = "CREATE", entityType = "PAYMENT")
    @Transactional
    public Long createManualPayment(dinhgiang.dev.hungthinh.models.dtos.payments.ManualPaymentRequest request) {
        assertCurrentUserCanRecordManualPayment();

        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new UserMessageException("Hóa đơn không tồn tại"));

        if (invoice.getInvoiceStatus() == InvoiceStatus.PAID) {
            throw new UserMessageException("Hóa đơn này đã được thanh toán");
        }

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(request.getPaymentMethod());
        } catch (IllegalArgumentException e) {
            throw new UserMessageException("Phương thức thanh toán không hợp lệ");
        }

        if (method != PaymentMethod.CASH) {
            throw new UserMessageException("Chỉ hỗ trợ ghi nhận tiền mặt");
        }

        // Resolve current admin user
        String[] payer = resolveCurrentPayer();

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(invoice.getTotalAmount());
        payment.setPaymentDateTime(LocalDateTime.now());
        payment.setPaymentMethod(method);
        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPayerName(payer[0]);
        payment.setPayerUsername(payer[1]);
        payment.setResponseCode("0"); // Manual success

        if (request.getTransactionNo() != null && !request.getTransactionNo().isBlank()) {
            payment.setTransactionNo(request.getTransactionNo().trim());
        }
        if (request.getBankCode() != null && !request.getBankCode().isBlank()) {
            payment.setBankCode(request.getBankCode().trim());
        }
        // Store note in payDate field (reuse existing field)
        if (request.getNote() != null && !request.getNote().isBlank()) {
            payment.setPayDate(request.getNote().trim());
        }

        paymentRepository.save(payment);

        // Update invoice status to PAID
        invoice.setInvoiceStatus(InvoiceStatus.PAID);
        invoiceRepository.save(invoice);

        return payment.getId();
    }

    @Auditable(action = "CREATE", entityType = "PAYMENT")
    @Transactional
    public Long requestCashPayment(dinhgiang.dev.hungthinh.models.dtos.payments.ManualPaymentRequest request) {
        PaymentMethod method = resolveCashMethod(request.getPaymentMethod());
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new UserMessageException("Hóa đơn không tồn tại"));

        assertCurrentUserCanAccessInvoice(invoice);

        if (invoice.getInvoiceStatus() == InvoiceStatus.PAID) {
            throw new UserMessageException("Hóa đơn này đã được thanh toán");
        }

        boolean hasPendingCashPayment = paymentRepository.countPendingPayment(
                invoice.getId(),
                method,
                PaymentStatus.PENDING
        ) > 0;
        if (hasPendingCashPayment) {
            throw new UserMessageException("Hóa đơn đã có đề xuất thanh toán tiền mặt đang chờ xác nhận");
        }

        String[] payer = resolveCurrentPayer();

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(invoice.getTotalAmount());
        payment.setPaymentMethod(method);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setPayerName(payer[0]);
        payment.setPayerUsername(payer[1]);
        payment.setResponseCode("PENDING");
        if (request.getNote() != null && !request.getNote().isBlank()) {
            payment.setPayDate(request.getNote().trim());
        }

        paymentRepository.save(payment);
        return payment.getId();
    }

    @Auditable(action = "CONFIRM", entityType = "PAYMENT")
    @Transactional
    public Long confirmCashPayment(Long paymentId) {
        assertCurrentUserCanRecordManualPayment();

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new UserMessageException("Thanh toán không tồn tại"));

        if (payment.getPaymentMethod() != PaymentMethod.CASH) {
            throw new UserMessageException("Chỉ có thể xác nhận đề xuất thanh toán tiền mặt");
        }
        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new UserMessageException("Đề xuất thanh toán không ở trạng thái chờ xác nhận");
        }

        Invoice invoice = payment.getInvoice();
        if (invoice == null) {
            throw new UserMessageException("Thanh toán không gắn với hóa đơn");
        }
        if (invoice.getInvoiceStatus() == InvoiceStatus.PAID) {
            throw new UserMessageException("Hóa đơn này đã được thanh toán");
        }

        payment.setPaymentStatus(PaymentStatus.SUCCESS);
        payment.setPaymentDateTime(LocalDateTime.now());
        payment.setResponseCode("0");
        invoice.setInvoiceStatus(InvoiceStatus.PAID);

        paymentRepository.save(payment);
        invoiceRepository.save(invoice);
        return payment.getId();
    }

    private void assertCurrentUserCanRecordManualPayment() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));

        if (!isAdmin) {
            throw new UserMessageException("Chỉ quản trị viên mới được ghi nhận thanh toán tiền mặt");
        }
    }

    private PaymentMethod resolveCashMethod(String paymentMethod) {
        if (paymentMethod == null || paymentMethod.isBlank()) {
            throw new UserMessageException("Phương thức thanh toán không được để trống");
        }

        PaymentMethod method;
        try {
            method = PaymentMethod.valueOf(paymentMethod.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new UserMessageException("Phương thức thanh toán không hợp lệ");
        }

        if (method != PaymentMethod.CASH) {
            throw new UserMessageException("Chỉ hỗ trợ đề xuất thanh toán tiền mặt");
        }
        return method;
    }

    private void assertCurrentUserCanAccessInvoice(Invoice invoice) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new UserMessageException("Bạn chưa đăng nhập");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (isAdmin) {
            return;
        }

        String username = authentication.getName();
        Resident resident = residentRepository.findByUsername(username)
                .orElseThrow(() -> new UserMessageException("Cư dân không tồn tại"));
        if (resident.getApartment() == null
                || invoice.getApartment() == null
                || !resident.getApartment().getId().equals(invoice.getApartment().getId())) {
            throw new UserMessageException("Bạn không có quyền tạo đề xuất thanh toán cho hóa đơn này");
        }
    }
}
