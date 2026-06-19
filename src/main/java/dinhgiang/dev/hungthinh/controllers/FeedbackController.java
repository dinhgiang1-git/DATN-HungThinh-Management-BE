package dinhgiang.dev.hungthinh.controllers;

import dinhgiang.dev.hungthinh.models.dtos.feedbacks.FeedbackCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.feedbacks.FeedbackGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.feedbacks.FeedbackUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.enums.FeedbackStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.FeedbackType;
import dinhgiang.dev.hungthinh.models.entities.global.ApiResponse;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import dinhgiang.dev.hungthinh.services.interfaces.IFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/feedbacks")
@RequiredArgsConstructor
public class FeedbackController extends BaseController {
    private final IFeedbackService feedbackService;

    @Operation(summary = "Lấy danh sách phản hồi")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<PageResponse<FeedbackGetResponse>>> getAllFeedbacks(
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng phần tử trong một trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trạng thái phản hồi: PENDING, IN_PROGRESS, RESOLVED, CLOSED") @RequestParam(required = false) String feedbackStatus,
            @Parameter(description = "Loại phản hồi: GENERAL, MAINTENANCE, SERVICE") @RequestParam(required = false) String feedbackType,
            @Parameter(description = "Sẵp xếp theo id") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Theo thứ tự asc") @RequestParam(defaultValue = "asc") String direction,
            @Parameter(description = "Từ khóa tìm kiếm (Theo Title, Content, Response)") @RequestParam(name = "keyword", required = false) String keyword,
            @Parameter(description = "Lọc theo căn hộ") @RequestParam(name = "apartmentId", required = false) Long apartmentId
    ) {
        FeedbackStatus statusEnum = (feedbackStatus != null && !feedbackStatus.isBlank()) ? FeedbackStatus.valueOf(feedbackStatus) : null;
        FeedbackType typeEnum = (feedbackType != null && !feedbackType.isBlank()) ? FeedbackType.valueOf(feedbackType) : null;

        return success(feedbackService.getAllFeedbacks(page, size, statusEnum, typeEnum, sortBy, direction, keyword, apartmentId), "Lấy danh sách phản hồi thành công");
    }

    @Operation(summary = "Lấy phản hồi theo ID")
    @GetMapping("/{feedbackId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<FeedbackGetResponse>> getFeedbackById(@PathVariable Long feedbackId) {
        return success(feedbackService.getFeedbackById(feedbackId), "Lấy phản hồi thành công");
    }

    @Operation(summary = "Tạo mới phản hồi phản hồi")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT') or hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<Long>> createFeedback(@RequestBody FeedbackCreateRequest apiRequest) {
        return success(feedbackService.createFeedback(apiRequest), "Tạo mới phản hồi thành công");
    }

    @Operation(summary = "Cập nhật phản hồi")
    @PatchMapping("/{feedbackId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<Long>> updateFeedback(@PathVariable Long feedbackId, @RequestBody FeedbackUpdateRequest apiRequest) {
        return success(feedbackService.updateFeedback(feedbackId, apiRequest), "Cập nhật phản hồi thành công");
    }

    @Operation(summary = "Xóa phản hồi")
    @DeleteMapping("/{feedbackId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<Void>> deleteFeedback(@PathVariable Long feedbackId) {
        return success(feedbackService.deleteFeedback(feedbackId), "Xóa phản hồi thành công");
    }
}
