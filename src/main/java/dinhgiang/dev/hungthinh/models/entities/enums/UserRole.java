package dinhgiang.dev.hungthinh.models.entities.enums;

import lombok.Getter;

/**
 * Enum vai trò người dùng trong hệ thống.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Getter
public enum UserRole {
    ADMIN ("Quản trị viên"),
    RESIDENT ("Cư dân"),
    TECHNICIAN ("Kỹ thuật viên");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

}
