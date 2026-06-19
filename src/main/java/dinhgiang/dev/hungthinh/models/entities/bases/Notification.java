package dinhgiang.dev.hungthinh.models.entities.bases;

import dinhgiang.dev.hungthinh.models.entities.enums.TargetType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity đại diện cho thông báo gửi đến cư dân.
 * Hỗ trợ gửi thông báo theo nhiều phạm vi: tất cả, theo block, hoặc theo căn hộ cụ thể.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "notifications")
@Data
public class Notification extends BaseEntity {

    /** Tiêu đề thông báo */
    @Column(name = "title", nullable = false)
    private String title;

    /** Nội dung chi tiết thông báo */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /** Thời gian gửi thông báo */
    @Column(name = "send_time", nullable = false)
    private LocalDateTime sendTime;

    /** Phạm vi gửi thông báo: ALL, BLOCK, APARTMENT */
    @Column(name = "target_type")
    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    /** Block nhận thông báo (khi targetType = BLOCK) */
    @Column(name = "block")
    private String block;

    /** ID căn hộ nhận thông báo (khi targetType = APARTMENT) */
    @Column(name = "notification_apartment_id")
    private Long apartmentId;

    // ==================== Quan hệ (Relationships) ====================

    /** Người gửi thông báo (Admin/Kỹ thuật viên, quan hệ N-1) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    /** Danh sách người nhận thông báo (quan hệ 1-N) */
    @OneToMany(mappedBy = "notification", cascade = CascadeType.ALL)
    private List<NotificationReceiver> receivers;

}
