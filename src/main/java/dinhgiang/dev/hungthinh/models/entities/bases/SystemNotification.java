package dinhgiang.dev.hungthinh.models.entities.bases;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho thông báo hệ thống gửi đến cư dân cụ thể.
 * Khác với Notification (thông báo chung), SystemNotification là thông báo tự động
 * từ hệ thống (VD: nhắc đóng phí, xác nhận thanh toán).
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Entity
@Table(name = "system_notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemNotification {

    /** Khóa chính tự động tăng */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tiêu đề thông báo hệ thống */
    @Column(nullable = false)
    private String title;

    /** Nội dung chi tiết thông báo */
    @Column(columnDefinition = "TEXT")
    private String content;

    /** Thời gian gửi thông báo */
    @Column(nullable = false)
    private LocalDateTime sendTime;

    /** Trạng thái đã đọc */
    @Column(nullable = false)
    private Boolean isRead;

    /** Thời điểm đọc thông báo */
    private LocalDateTime readAt;

    // ==================== Quan hệ (Relationships) ====================

    /** Cư dân nhận thông báo (quan hệ N-1) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

    /** Căn hộ liên quan (quan hệ N-1) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id")
    private Apartment apartment;

    /** ID thông báo gốc (nếu được tạo từ Notification) */
    @Column(name = "notification_id")
    private Long notificationId;
}
