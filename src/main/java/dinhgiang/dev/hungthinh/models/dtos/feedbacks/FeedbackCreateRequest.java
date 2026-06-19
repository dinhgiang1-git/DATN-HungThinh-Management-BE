package dinhgiang.dev.hungthinh.models.dtos.feedbacks;

import dinhgiang.dev.hungthinh.models.entities.enums.FeedbackStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.FeedbackType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FeedbackCreateRequest {
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;
    
    @NotBlank(message = "Nội dung không được để trống")
    private String content;

    private FeedbackType feedbackType;
    
    private FeedbackStatus feedbackStatus;

    @NotNull(message = "Số nhà không được để trống")
    private Long apartmentId;
    
    private Long senderId;
    
    private Long deviceId;
}
