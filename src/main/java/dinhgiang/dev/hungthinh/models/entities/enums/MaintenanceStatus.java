package dinhgiang.dev.hungthinh.models.entities.enums;

import lombok.Getter;

/**
 * Enum trạng thái công việc bảo trì.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Getter
public enum MaintenanceStatus {
    SCHEDULED("Đã lên lịch"),
    IN_PROGRESS("Đang thực hiện"),
    COMPLETED("Đã hoàn thành"),
    CANCELLED("Đã hủy");

    private final String displayName;
    MaintenanceStatus(String displayName) {
        this.displayName = displayName;
    }
}
