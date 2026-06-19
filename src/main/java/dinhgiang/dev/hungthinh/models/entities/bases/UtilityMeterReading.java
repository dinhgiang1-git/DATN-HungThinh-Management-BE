package dinhgiang.dev.hungthinh.models.entities.bases;

import dinhgiang.dev.hungthinh.models.entities.enums.MeterReadingSource;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entity đại diện cho bản ghi chỉ số điện nước hàng tháng.
 * Lưu trữ chỉ số đọc được từ công tơ điện/nước, nguồn dữ liệu và file minh chứng.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Entity
@Getter
@Setter
@Table(name = "utility_meter_readings")
public class UtilityMeterReading extends BaseEntity {

    // ==================== Quan hệ (Relationships) ====================

    /** Căn hộ được ghi chỉ số (quan hệ N-1) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;

    /** Người ghi chỉ số (Admin/Kỹ thuật viên, quan hệ N-1) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_id")
    private User recordedBy;

    /** Hóa đơn liên kết (quan hệ N-1) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    // ==================== Thông tin chỉ số ====================

    /** Kỳ ghi chỉ số (định dạng MM/YYYY) */
    @Column(name = "billing_period", length = 7)
    private String billingPeriod;

    /** Chỉ số điện kỳ trước */
    @Column(name = "electric_previous_reading", precision = 14, scale = 2)
    private BigDecimal electricPreviousReading;

    /** Chỉ số điện kỳ hiện tại */
    @Column(name = "electric_current_reading", precision = 14, scale = 2)
    private BigDecimal electricCurrentReading;

    /** Lượng điện tiêu thụ (kWh) = hiện tại - kỳ trước */
    @Column(name = "electric_quantity", precision = 14, scale = 2)
    private BigDecimal electricQuantity;

    /** Chỉ số nước kỳ trước */
    @Column(name = "water_previous_reading", precision = 14, scale = 2)
    private BigDecimal waterPreviousReading;

    /** Chỉ số nước kỳ hiện tại */
    @Column(name = "water_current_reading", precision = 14, scale = 2)
    private BigDecimal waterCurrentReading;

    /** Lượng nước tiêu thụ (m³) = hiện tại - kỳ trước */
    @Column(name = "water_quantity", precision = 14, scale = 2)
    private BigDecimal waterQuantity;

    /** Nguồn dữ liệu: MANUAL (nhập tay), API (từ hệ thống bên ngoài) */
    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private MeterReadingSource source;

    // ==================== File minh chứng ====================

    /** Đường dẫn file minh chứng trên server */
    @Column(name = "evidence_file_path")
    private String evidenceFilePath;

    /** Tên file gốc khi upload */
    @Column(name = "evidence_original_file_name")
    private String evidenceOriginalFileName;

    /** Loại nội dung file (MIME type) */
    @Column(name = "evidence_content_type")
    private String evidenceContentType;

    /** Đường dẫn ảnh minh chứng chỉ số điện trên server */
    @Column(name = "electric_evidence_file_path")
    private String electricEvidenceFilePath;

    /** Tên file gốc của ảnh minh chứng chỉ số điện */
    @Column(name = "electric_evidence_original_file_name")
    private String electricEvidenceOriginalFileName;

    /** Loại nội dung ảnh minh chứng chỉ số điện */
    @Column(name = "electric_evidence_content_type")
    private String electricEvidenceContentType;

    /** Đường dẫn ảnh minh chứng chỉ số nước trên server */
    @Column(name = "water_evidence_file_path")
    private String waterEvidenceFilePath;

    /** Tên file gốc của ảnh minh chứng chỉ số nước */
    @Column(name = "water_evidence_original_file_name")
    private String waterEvidenceOriginalFileName;

    /** Loại nội dung ảnh minh chứng chỉ số nước */
    @Column(name = "water_evidence_content_type")
    private String waterEvidenceContentType;

    /** Ghi chú bổ sung */
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}
