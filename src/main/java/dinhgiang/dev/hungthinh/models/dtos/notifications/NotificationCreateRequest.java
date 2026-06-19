package dinhgiang.dev.hungthinh.models.dtos.notifications;

import dinhgiang.dev.hungthinh.models.entities.enums.TargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCreateRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;
    private String content;
    @NotNull(message = "Kiểu thông báo không được để trống")
    private TargetType targetType;
    @NotNull(message = "Người gửi không được để trống")
    private Long senderId;
    //optioal
    private String block;
    private Long apartmentId;
}
