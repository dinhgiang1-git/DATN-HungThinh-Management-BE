package dinhgiang.dev.hungthinh.models.dtos.notifications;

import dinhgiang.dev.hungthinh.models.entities.enums.TargetType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NotificationUpdateRequest {
    private String title;
    private String content;
    private TargetType targetType;
}
