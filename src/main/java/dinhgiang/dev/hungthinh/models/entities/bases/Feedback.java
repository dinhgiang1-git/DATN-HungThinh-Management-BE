package dinhgiang.dev.hungthinh.models.entities.bases;

import dinhgiang.dev.hungthinh.models.entities.bases.Device;
import dinhgiang.dev.hungthinh.models.entities.enums.FeedbackStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.FeedbackType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity đại diện cho phản hồi/góp ý của cư dân.
 * Lưu trữ nội dung phản hồi, loại phản hồi, trạng thái xử lý và phản hồi từ admin.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Table(name = "feedbacks")
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Feedback extends BaseEntity {

    /** Tiêu đề phản hồi */
    @Column(name = "title", nullable = false)
    private String title;

    /** Nội dung chi tiết phản hồi */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /** Loại phản hồi: GENERAL, MAINTENANCE, COMPLAINT, SUGGESTION */
    @Column(name = "feedback_type", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FeedbackType feedbackType = FeedbackType.GENERAL;

    /** Trạng thái xử lý: PENDING, IN_PROGRESS, RESOLVED, REJECTED */
    @Column(name = "feedback_status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FeedbackStatus feedbackStatus = FeedbackStatus.PENDING;

    /** Phản hồi/trả lời từ admin */
    @Column(name = "response", columnDefinition = "TEXT")
    private String response;

    /** Trạng thái đã đọc của phản hồi */
    @Column(name = "is_read")
    @Builder.Default
    private boolean isRead = false;

    // ==================== Quan hệ (Relationships) ====================

    /** Căn hộ gửi phản hồi (quan hệ N-1) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    @EqualsAndHashCode.Exclude
    private Apartment apartment;

    /** Cư dân gửi phản hồi (quan hệ N-1) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    @EqualsAndHashCode.Exclude
    private Resident sender;

    /** Thiết bị liên quan (nếu phản hồi về thiết bị, quan hệ N-1) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    @EqualsAndHashCode.Exclude
    private Device device;
}
