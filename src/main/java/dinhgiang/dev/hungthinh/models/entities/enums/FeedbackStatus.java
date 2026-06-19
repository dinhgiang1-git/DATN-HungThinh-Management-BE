package dinhgiang.dev.hungthinh.models.entities.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum trạng thái xử lý phản hồi.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Getter
@AllArgsConstructor
public enum FeedbackStatus {
    PENDING ("Chờ xử lý"),
    IN_PROGRESS ("Đang xử lý"),
    RESOLVED ("Đã xử lý"),
    CLOSED ("Đóng");

    private final String displayName;
}
