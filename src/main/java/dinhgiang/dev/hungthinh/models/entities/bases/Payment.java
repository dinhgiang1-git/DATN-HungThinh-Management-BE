package dinhgiang.dev.hungthinh.models.entities.bases;

import dinhgiang.dev.hungthinh.models.entities.enums.PaymentMethod;
import dinhgiang.dev.hungthinh.models.entities.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho giao dịch thanh toán hóa đơn.
 * Lưu trữ thông tin thanh toán: số tiền, phương thức, trạng thái và thông tin giao dịch MoMo/VNPay.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "payments")
public class Payment extends BaseEntity {

        /** Số tiền thanh toán (VND) */
        @Column(name = "amount")
        private BigDecimal amount;

        /** Thời điểm thanh toán */
        @Column(name = "payment_date_time")
        private LocalDateTime paymentDateTime;

        /** Trạng thái thanh toán: PENDING, SUCCESS, FAILED */
        @Column(name = "status")
        @Enumerated(EnumType.STRING)
        private PaymentStatus paymentStatus;

        /** Phương thức thanh toán: VNPAY, MOMO, CASH */
        @Column(name = "payment_method", columnDefinition = "VARCHAR(20)")
        @Enumerated(EnumType.STRING)
        private PaymentMethod paymentMethod;

        /** Mã tham chiếu giao dịch (vnp_TxnRef) */
        @Column(name = "txnRef")
        private String txnRef;

        /** Mã giao dịch từ cổng thanh toán (duy nhất) */
        @Column(name = "transaction_no", unique = true)
        private String transactionNo;

        /** Mã phản hồi từ cổng thanh toán */
        @Column(name = "response_code")
        private String responseCode;

        /** Mã ngân hàng */
        @Column(name = "bank_code")
        private String bankCode;

        /** Ngày thanh toán (từ cổng thanh toán) */
        @Column(name = "pay_date")
        private String payDate;

        /** Tên người thanh toán */
        @Column(name = "payer_name")
        private String payerName;

        /** Username người thanh toán */
        @Column(name = "payer_username")
        private String payerUsername;

        // ==================== Quan hệ (Relationships) ====================

        /** Hóa đơn được thanh toán (quan hệ N-1) */
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "invoice_id")
        private Invoice invoice;
}
