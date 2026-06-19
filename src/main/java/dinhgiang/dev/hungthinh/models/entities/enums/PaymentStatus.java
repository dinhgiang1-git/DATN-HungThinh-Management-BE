package dinhgiang.dev.hungthinh.models.entities.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum trạng thái giao dịch thanh toán.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Getter
@AllArgsConstructor
public enum PaymentStatus {
    PENDING ("Đang chờ"),
    SUCCESS ("Thành công"),
    FAILED ("Thất bại");

    private final String displayName;
}
