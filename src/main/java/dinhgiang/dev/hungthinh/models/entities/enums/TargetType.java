package dinhgiang.dev.hungthinh.models.entities.enums;

import lombok.Getter;

/**
 * Enum phạm vi đối tượng nhận thông báo.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Getter
public enum TargetType {
    ALL("Tất cả"),
    BLOCK("Theo block"),
    APARTMENT("Căn hộ cụ thể");

    private final String displayName;

    TargetType(String displayName) {
        this.displayName = displayName;
    }
}