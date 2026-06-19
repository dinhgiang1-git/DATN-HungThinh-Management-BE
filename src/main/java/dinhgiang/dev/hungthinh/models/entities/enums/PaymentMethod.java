package dinhgiang.dev.hungthinh.models.entities.enums;

import lombok.Getter;

/**
 * Enum phương thức thanh toán được hỗ trợ.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Getter
public enum PaymentMethod {
    /** Thanh toán qua VNPay */
    VNPAY,
    /** Thanh toán qua MoMo */
    MOMO,
    /** Thanh toán tiền mặt */
    CASH;
}
