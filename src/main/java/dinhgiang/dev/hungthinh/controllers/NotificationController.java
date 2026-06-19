package dinhgiang.dev.hungthinh.controllers;

import dinhgiang.dev.hungthinh.models.dtos.notifications.NotificationCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.notifications.NotificationGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.notifications.NotificationUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.enums.TargetType;
import dinhgiang.dev.hungthinh.models.entities.global.ApiResponse;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import dinhgiang.dev.hungthinh.services.interfaces.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import dinhgiang.dev.hungthinh.models.dtos.notifications.ReceiverGetResponse;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController extends BaseController {
    private final INotificationService notificationService;

    @Operation(summary = "Lấy danh sách thông báo")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<PageResponse<NotificationGetResponse>>> getAllNotifications(
            @Parameter(description = "Số trang") @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Số lượng phần tử trong một trang") @RequestParam(name = "size", defaultValue = "10") int size,
            @Parameter(description = "Đối tượng thông báo: ALL, BLOCK, APARTMENT") @RequestParam(name = "targetType", required = false) TargetType targetType,
            @Parameter(description = "Sẵp xếp theo id") @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @Parameter(description = "Theo thứ tự asc") @RequestParam(name = "direction", defaultValue = "asc") String direction,
            @Parameter(description = "Từ khóa tìm kiếm (Theo Title, Content, SendTime)") @RequestParam(name = "keyword", required = false) String keyword
    ) {
        return success(notificationService.getAllNotifications(page, size, targetType, sortBy, direction, keyword), "Lấy danh sách thông báo thành công");
    }

    @Operation(summary = "Lấy thông báo theo ID")
    @GetMapping("/{notificationId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<NotificationGetResponse>> getNotification(@PathVariable Long notificationId) {
        return success(notificationService.getNotificationById(notificationId), "Lấy thông báo thành công");
    }

    @Operation(summary = "Lấy danh sách người nhận thông báo (phân trang)")
    @GetMapping("/{notificationId}/receivers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<PageResponse<ReceiverGetResponse>>> getNotificationReceivers(
            @PathVariable Long notificationId,
            @Parameter(description = "Số trang") @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Số lượng") @RequestParam(name = "size", defaultValue = "10") int size,
            @Parameter(description = "Tìm kiếm theo tên/email") @RequestParam(name = "keyword", required = false) String keyword,
            @Parameter(description = "Lọc theo trạng thái đọc") @RequestParam(name = "isRead", required = false) Boolean isRead
    ) {
        return success(notificationService.getReceiversByNotification(notificationId, page, size, keyword, isRead), "Lấy danh sách người nhận thành công");
    }

    @Operation(summary = "Lấy danh sách thông báo theo cư dân (chỉ những thông báo được gửi đến cư dân này)")
    @GetMapping("/resident/{residentId}")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<PageResponse<NotificationGetResponse>>> getNotificationsByResident(
            @PathVariable Long residentId,
            @Parameter(description = "Số trang") @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Số lượng") @RequestParam(name = "size", defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp theo") @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @Parameter(description = "Thứ tự") @RequestParam(name = "direction", defaultValue = "desc") String direction,
            @Parameter(description = "Từ khóa") @RequestParam(name = "keyword", required = false) String keyword
    ) {
        return success(notificationService.getNotificationsByResident(residentId, page, size, sortBy, direction, keyword), "Lấy thông báo cư dân thành công");
    }

    @Operation(summary = "Tạo mới thông báo thành công")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<Long>> createNotification(@Valid @RequestBody NotificationCreateRequest apiRequest) {
        return success(notificationService.createNotification(apiRequest), "Tạo mới thông báo thành công");
    }

    @Operation(summary = "Cập nhật thông báo (PATCH)")
    @PatchMapping("/{notificationId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<Long>> updateNotification(@PathVariable("notificationId") Long notificationId,
            @RequestBody NotificationUpdateRequest apiRequest) {
        return success(notificationService.updateNotification(notificationId, apiRequest), "Cập nhật thông báo thành công");
    }

    @Operation(summary = "Xóa thông báo theo ID")
    @DeleteMapping("/{notificationId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable("notificationId") Long notificationId) {
        return success(notificationService.deleteNotification(notificationId), "Xóa thành công thông báo");
    }

    @Operation(summary = "Đánh dấu thông báo đã đọc bởi cư dân")
    @PatchMapping("/{notificationId}/read/{residentId}")
    @PreAuthorize("hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long notificationId,
            @PathVariable Long residentId
    ) {
        notificationService.markAsRead(notificationId, residentId);
        return success(null, "Đã đánh dấu đọc");
    }
}
