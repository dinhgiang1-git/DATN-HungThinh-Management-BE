package dinhgiang.dev.hungthinh.models.entities.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum trạng thái hóa đơn.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@AllArgsConstructor
@Getter
public enum InvoiceStatus {
    PAID ("Đã thanh toán"),
    UNPAID ("Chưa thanh toán"),
    OVERDUE ("Quá hạn");

    private final String displayName;
}
