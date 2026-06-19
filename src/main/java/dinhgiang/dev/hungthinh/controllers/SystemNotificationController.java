package dinhgiang.dev.hungthinh.controllers;

import dinhgiang.dev.hungthinh.models.dtos.systemnotifications.SystemNotificationGetResponse;
import dinhgiang.dev.hungthinh.models.entities.global.ApiResponse;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import dinhgiang.dev.hungthinh.services.implement.SystemNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/system-notifications")
@RestController
@RequiredArgsConstructor
public class SystemNotificationController extends BaseController {

    private final SystemNotificationService systemNotificationService;

    @GetMapping("/resident/{residentId}")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<PageResponse<SystemNotificationGetResponse>>> getByResident(
            @PathVariable Long residentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return success(systemNotificationService.getByResident(residentId, page, size),
                "Lấy thông báo hệ thống thành công");
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        systemNotificationService.markAsRead(id);
        return success(null, "Đã đánh dấu đã đọc");
    }

    @PatchMapping("/resident/{residentId}/read-all")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(@PathVariable Long residentId) {
        systemNotificationService.markAllAsRead(residentId);
        return success(null, "Đã đánh dấu tất cả thông báo là đã đọc");
    }

    @GetMapping("/resident/{residentId}/unread-count")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(@PathVariable Long residentId) {
        return success(systemNotificationService.countUnread(residentId), "OK");
    }
}
