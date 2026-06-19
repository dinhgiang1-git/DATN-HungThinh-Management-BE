package dinhgiang.dev.hungthinh.models.entities.bases;

import dinhgiang.dev.hungthinh.models.entities.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Entity đại diện cho hóa đơn phí dịch vụ chung cư.
 * Lưu trữ chi tiết các khoản phí: điện, nước, quản lý, gửi xe và các khoản khác.
 * Ràng buộc unique theo cặp (apartment_id, billing_period) để tránh trùng lặp hóa đơn.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Entity
@Table(
        name = "invoices",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_invoice_apartment_billing_period",
                columnNames = {"apartment_id", "billing_period"}
        )
)
@Data
public class Invoice extends BaseEntity{

    /** Số hóa đơn (duy nhất) */
    @Column(name = "invoice_number", unique = true, nullable = false)
    @EqualsAndHashCode.Include
    private String invoiceNumber;

    /** Ngày hết hạn thanh toán */
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    /** Kỳ thanh toán (định dạng MM/YYYY) */
    @Column(name = "billing_period", length = 7)
    private String billingPeriod;

    // ==================== Phí điện ====================

    /** Tổng phí điện (VND) */
    @Column(name = "electric_fee")
    private BigDecimal electricFee;

    /** Chỉ số điện kỳ trước */
    @Column(name = "electric_previous_reading", precision = 14, scale = 2)
    private BigDecimal electricPreviousReading;

    /** Chỉ số điện kỳ hiện tại */
    @Column(name = "electric_current_reading", precision = 14, scale = 2)
    private BigDecimal electricCurrentReading;

    /** Lượng điện tiêu thụ (kWh) */
    @Column(name = "electric_quantity", precision = 14, scale = 2)
    private BigDecimal electricQuantity;

    // ==================== Phí nước ====================

    /** Tổng phí nước (VND) */
    @Column(name = "water_fee")
    private BigDecimal waterFee;

    /** Chỉ số nước kỳ trước */
    @Column(name = "water_previous_reading", precision = 14, scale = 2)
    private BigDecimal waterPreviousReading;

    /** Chỉ số nước kỳ hiện tại */
    @Column(name = "water_current_reading", precision = 14, scale = 2)
    private BigDecimal waterCurrentReading;

    /** Lượng nước tiêu thụ (m³) */
    @Column(name = "water_quantity", precision = 14, scale = 2)
    private BigDecimal waterQuantity;

    // ==================== Các khoản phí khác ====================

    /** Phí quản lý chung cư (VND) */
    @Column(name = "management_fee")
    private BigDecimal managementFee;

    /** Phí gửi xe (VND) */
    @Column(name = "parking_fee")
    private BigDecimal parkingFee;

    /** Phí khác (VND) */
    @Column(name = "other_fee")
    private BigDecimal otherFee;

    /** Tổng số tiền hóa đơn (VND) */
    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    /** Mô tả chi tiết phí khác */
    @Column(name = "description_other_fee", columnDefinition = "TEXT")
    private String descriptionOtherFee;

    /** Trạng thái hóa đơn: PAID (đã thanh toán), UNPAID (chưa thanh toán) */
    @Column(name = "invoice_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private InvoiceStatus invoiceStatus;

    // ==================== Quan hệ (Relationships) ====================

    /** Người tạo hóa đơn (quan hệ N-1 với User) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    /** Căn hộ liên quan đến hóa đơn (quan hệ N-1 với Apartment) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    /** Danh sách thanh toán cho hóa đơn (quan hệ 1-N) */
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
    private List<Payment> payments;

}
