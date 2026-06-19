package dinhgiang.dev.hungthinh.models.entities.enums;

import lombok.Getter;

/**
 * Enum trạng thái căn hộ trong hệ thống chung cư.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Getter
public enum ApartmentStatus {
    OCCUPIED ("Đang ở"),
    VACANT ("Đang trống"),
    UNDER_MAINTENANCE ("Đang bảo trì");

    private final String displayName;
    ApartmentStatus(String displayName) {
        this.displayName = displayName;
    }

}
