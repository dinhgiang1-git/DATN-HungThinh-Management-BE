package dinhgiang.dev.hungthinh.models.dtos.payments;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ManualPaymentRequest {

    @NotNull(message = "invoiceId không được để trống")
    private Long invoiceId;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod; // CASH hoặc BANK_TRANSFER

    private String note; // Ghi chú

    private String transactionNo; // Mã giao dịch (cho chuyển khoản)

    private String bankCode; // Tên ngân hàng (cho chuyển khoản)
}
