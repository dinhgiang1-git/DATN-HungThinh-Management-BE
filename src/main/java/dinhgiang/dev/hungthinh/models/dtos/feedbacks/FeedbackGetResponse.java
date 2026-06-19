package dinhgiang.dev.hungthinh.models.dtos.feedbacks;

import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentShortGetResponse;
import dinhgiang.dev.hungthinh.models.entities.enums.FeedbackStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.FeedbackType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeedbackGetResponse {
    private Long feedbackId;
    private String title;
    private String content;
    private FeedbackType feedbackType;
    private FeedbackStatus feedbackStatus;
    private String response;
    private String senderName;
    private ApartmentShortGetResponse apartment;
    private Long deviceId;
    private String deviceName;
    private java.time.LocalDateTime createdAt;
}
