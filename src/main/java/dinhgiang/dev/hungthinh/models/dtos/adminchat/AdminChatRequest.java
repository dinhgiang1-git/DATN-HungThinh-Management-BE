package dinhgiang.dev.hungthinh.models.dtos.adminchat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AdminChatRequest {
    @NotBlank(message = "Nội dung câu hỏi không được để trống")
    private String message;

    private List<ChatMessage> messages = new ArrayList<>();

    @Data
    public static class ChatMessage {
        private String role;
        private String content;
    }
}
