package dinhgiang.dev.hungthinh.models.dtos.systemnotifications;

import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentShortGetResponse;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SystemNotificationGetResponse {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime sendTime;
    private Boolean isRead;
    private LocalDateTime readAt;
    private ApartmentShortGetResponse apartment;
    private String senderName;
}
