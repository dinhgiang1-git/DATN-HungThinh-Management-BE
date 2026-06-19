package dinhgiang.dev.hungthinh.models.entities.enums;

import lombok.Getter;

/**
 * Enum loại hợp đồng.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Getter
public enum ContractType {
    RENT ("Thuê"),
    PURCHASE ("Mua bán"),
    SERVICE ("Dịch vụ");

    private final String displayName;
    ContractType(String displayName) {
        this.displayName = displayName;
    }

}
