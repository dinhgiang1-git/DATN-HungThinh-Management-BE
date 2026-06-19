package dinhgiang.dev.hungthinh.models.dtos.feedbacks;

import dinhgiang.dev.hungthinh.models.entities.enums.FeedbackStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FeedbackUpdateRequest {
    private String title;
    private String content;
    private FeedbackStatus feedbackStatus;
    private String response;
    private Long apartmentId;
}
