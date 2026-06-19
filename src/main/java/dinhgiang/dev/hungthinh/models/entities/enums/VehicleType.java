package dinhgiang.dev.hungthinh.models.entities.enums;

import lombok.Getter;

/**
 * Enum loại phương tiện gửi xe tại chung cư.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Getter
public enum VehicleType {
    MOTORBIKE("Xe máy"),
    CAR("Ô tô"),
    BICYCLE("Xe đạp"),
    ELECTRIC_MOTORBIKE("Xe máy điện");

    private final String displayName;
    VehicleType(String displayName) {
        this.displayName = displayName;
    }
}
