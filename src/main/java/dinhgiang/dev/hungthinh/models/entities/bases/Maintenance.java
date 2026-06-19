package dinhgiang.dev.hungthinh.models.entities.bases;

import dinhgiang.dev.hungthinh.models.entities.enums.MaintenanceStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Entity đại diện cho công việc bảo trì trong chung cư.
 * Lưu trữ thông tin bảo trì: thời gian, chi phí, trạng thái và kỹ thuật viên phụ trách.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "maintenances")
@Data
public class Maintenance extends BaseEntity {

    /** Ngày hoàn thành bảo trì */
    @Column(name = "completed_date")
    private LocalDate completedDate;

    /** Ngày bắt đầu bảo trì */
    @Column(name = "started_date")
    private LocalDate startedDate;

    /** Chi phí bảo trì (VND) */
    @Column(name = "cost", precision = 12, scale = 2)
    private BigDecimal cost;

    /** Mô tả công việc bảo trì */
    @Column(name = "description")
    private String description;

    /** Trạng thái bảo trì: PENDING, IN_PROGRESS, COMPLETED, CANCELLED */
    @Column(name = "maintenance_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private MaintenanceStatus maintenanceStatus;

    // ==================== Quan hệ (Relationships) ====================

    /** Thiết bị cần bảo trì (quan hệ N-1) */
    @ManyToOne
    @JoinColumn(name = "device_id")
    private Device device;

    /** Phản hồi liên quan tạo ra yêu cầu bảo trì (quan hệ N-1) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id")
    private Feedback feedback;

    /** Căn hộ liên quan đến bảo trì (quan hệ N-1) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id")
    private Apartment apartment;

    /** Danh sách kỹ thuật viên được phân công (quan hệ N-N) */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "maintenance_technicians",
            joinColumns = @JoinColumn(name = "maintenance_id"),
            inverseJoinColumns = @JoinColumn(name = "technician_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"maintenance_id", "technician_id"})
    )
    private List<User> technicians;

}
