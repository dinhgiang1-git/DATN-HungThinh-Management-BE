package dinhgiang.dev.hungthinh.models.dtos.notifications;

import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentShortGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.users.UserShortGetResponse;
import dinhgiang.dev.hungthinh.models.entities.enums.TargetType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class NotificationGetResponse {
    private Long notificationId;
    private String title;
    private String content;
    private LocalDateTime sendTime;
    private TargetType targetType;
    private String block;
    private Long apartmentId;
    private ApartmentShortGetResponse apartment;
    private UserShortGetResponse sender;
    private List<ReceiverGetResponse> receivers;
    private Long readCount;
    private Long totalReceivers;
}
