package dinhgiang.dev.hungthinh.models.entities.enums;

import lombok.Getter;

/**
 * Enum trạng thái thiết bị.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Getter
public enum DeviceStatus {
    ACTIVE ("Hoạt dộng"),
    INACTIVE ("Không hoạt động"),
    UNDER_MAINTENANCE ("Đang bảo trì"),
    BROKEN ("Hỏng");

    private final String displayName;
    DeviceStatus(String displayName) {
        this.displayName = displayName;
    }
}
