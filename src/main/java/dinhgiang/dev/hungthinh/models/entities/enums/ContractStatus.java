package dinhgiang.dev.hungthinh.models.entities.enums;

import lombok.Getter;

/**
 * Enum trạng thái hợp đồng.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Getter
public enum ContractStatus {
    ACTIVE ("Đang hiệu lực"),
    EXPIRED ("Hết hạn"),
    TERMINATED ("Đã chấm dứt");

    private final String displayName;
    ContractStatus(String displayName) {
        this.displayName = displayName;
    }

}
