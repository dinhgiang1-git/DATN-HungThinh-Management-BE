package dinhgiang.dev.hungthinh.controllers;

import dinhgiang.dev.hungthinh.models.dtos.adminchat.AdminChatRequest;
import dinhgiang.dev.hungthinh.models.dtos.adminchat.AdminChatResponse;
import dinhgiang.dev.hungthinh.models.entities.global.ApiResponse;
import dinhgiang.dev.hungthinh.services.implement.AdminChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin-chat")
@RequiredArgsConstructor
public class AdminChatController extends BaseController {
    private final AdminChatService adminChatService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdminChatResponse>> ask(@Valid @RequestBody AdminChatRequest request) {
        return success(adminChatService.ask(request.getMessage(), request.getMessages()), "Trả lời thành công");
    }
}
