package dinhgiang.dev.hungthinh.models.entities.bases;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho nhật ký kiểm toán (Audit Log) của hệ thống.
 * Ghi lại mọi thao tác CRUD quan trọng trên các entity: ai thực hiện, thao tác gì, lúc nào.
 * Sử dụng index trên các cột thường xuyên truy vấn để tối ưu hiệu suất.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_entity", columnList = "entityType, entityId"),
        @Index(name = "idx_audit_created", columnList = "createdAt"),
        @Index(name = "idx_audit_performer", columnList = "performedBy"),
        @Index(name = "idx_audit_action", columnList = "action")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    /** Khóa chính tự động tăng */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Loại thao tác: CREATE, UPDATE, DELETE */
    @Column(nullable = false, length = 20)
    private String action;

    /** Loại entity bị tác động: INVOICE, CONTRACT, VEHICLE, APARTMENT, RESIDENT, USER, PAYMENT */
    @Column(nullable = false, length = 50)
    private String entityType;

    /** ID của entity bị tác động */
    private Long entityId;

    /** Tên hiển thị của entity (VD: "HD-042026-001", "A101", "59A1-12345") */
    @Column(length = 255)
    private String entityName;

    /** Username người thực hiện thao tác */
    @Column(nullable = false, length = 100)
    private String performedBy;

    /** Vai trò người thực hiện: ADMIN, RESIDENT */
    @Column(length = 20)
    private String performerRole;

    /** Mô tả chi tiết về thao tác */
    @Column(columnDefinition = "TEXT")
    private String details;

    /** Phương thức HTTP: GET, POST, PUT, PATCH, DELETE */
    @Column(length = 10)
    private String httpMethod;

    /** Đường dẫn API được gọi */
    @Column(length = 255)
    private String requestPath;

    /** Địa chỉ IP của client */
    @Column(length = 45)
    private String ipAddress;

    /** Mã trạng thái HTTP phản hồi */
    private Integer statusCode;

    /** Thời gian tạo bản ghi audit (tự động gán) */
    @Column(name = "created_at", updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
