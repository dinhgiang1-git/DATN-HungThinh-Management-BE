package dinhgiang.dev.hungthinh.models.dtos.payments;

import dinhgiang.dev.hungthinh.models.entities.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Data
public class PaymentGetResponse {
    private Long paymentId;
    private BigDecimal amount;
    private LocalDateTime paymentDateTime;
    private PaymentStatus paymentStatus; // PENDING, SUCCESS, FAILED
    private String paymentMethod; // VNPAY
    private String transactionCode; // vnp_TxnRef
    private String payerName;
    private String payerPhoneNumber;

}
