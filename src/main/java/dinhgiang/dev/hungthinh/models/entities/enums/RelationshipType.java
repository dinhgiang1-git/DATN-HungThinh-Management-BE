package dinhgiang.dev.hungthinh.models.entities.enums;

import lombok.Getter;

/**
 * Enum mối quan hệ của cư dân với chủ hộ.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Getter
public enum RelationshipType {
    OWNER ("Chủ hộ"),
    SPOUSE ("Vợ / chồng"),
    CHILD ("Con"),
    PARENT ("Cha / mẹ"),
    RELATIVE ("Người thân"),
    TENANT ("Người thuê"),
    OTHER ("Khác");

    private final String displayName;

    RelationshipType(String displayName) {
        this.displayName = displayName;
    }

}
