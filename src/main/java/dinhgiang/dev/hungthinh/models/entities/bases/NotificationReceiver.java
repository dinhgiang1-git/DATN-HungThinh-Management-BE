package dinhgiang.dev.hungthinh.models.entities.bases;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho bản ghi người nhận thông báo.
 * Theo dõi trạng thái đọc của từng cư dân đối với mỗi thông báo.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@Entity
@Table(name = "notification_receiver")
@NoArgsConstructor
@AllArgsConstructor
public class NotificationReceiver extends BaseEntity {

    /** Thông báo liên quan (quan hệ N-1) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    /** Cư dân nhận thông báo (quan hệ N-1) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id")
    private Resident resident;

    /** Trạng thái đã đọc */
    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    /** Thời điểm đọc thông báo */
    @Column(name = "read_at")
    private LocalDateTime readAt;

}
