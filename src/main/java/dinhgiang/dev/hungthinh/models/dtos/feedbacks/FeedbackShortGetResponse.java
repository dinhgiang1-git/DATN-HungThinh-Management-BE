package dinhgiang.dev.hungthinh.models.dtos.feedbacks;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeedbackShortGetResponse {
    private Long feedbackId;
    private String title;
    private String content;
    private String senderName;
    private String phoneNumber;
    private String apartmentName;
}
