package dinhgiang.dev.hungthinh.models.dtos.notifications;

import dinhgiang.dev.hungthinh.models.dtos.residents.ResidentShortGetResponse;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReceiverGetResponse {
    private Long receiverNotificationId;
    private ResidentShortGetResponse resident;
    private Boolean isRead;
    private LocalDateTime readAt;
}
