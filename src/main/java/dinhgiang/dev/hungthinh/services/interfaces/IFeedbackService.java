package dinhgiang.dev.hungthinh.services.interfaces;

import dinhgiang.dev.hungthinh.models.dtos.feedbacks.FeedbackCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.feedbacks.FeedbackGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.feedbacks.FeedbackUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.enums.FeedbackStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.FeedbackType;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;

/**
 * Interface định nghĩa các nghiệp vụ liên quan đến quản lý Phản hồi/Góp ý.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface IFeedbackService {

    /**
     * Lấy danh sách phản hồi có phân trang và lọc.
     *
     * @param page           trang hiện tại
     * @param size           số lượng phần tử trên mỗi trang
     * @param feedbackStatus trạng thái xử lý
     * @param feedbackType   loại phản hồi
     * @param sortBy         trường cần sắp xếp
     * @param direction      hướng sắp xếp
     * @param keyword        từ khóa tìm kiếm
     * @param apartmentId    ID căn hộ (nếu cần lọc theo căn hộ)
     * @return PageResponse chứa danh sách FeedbackGetResponse
     */
    PageResponse<FeedbackGetResponse> getAllFeedbacks(int page, int size, FeedbackStatus feedbackStatus, FeedbackType feedbackType, String sortBy, String direction, String keyword, Long apartmentId);

    /**
     * Lấy thông tin chi tiết một phản hồi.
     *
     * @param feedbackId ID của phản hồi
     * @return FeedbackGetResponse chứa thông tin phản hồi
     */
    FeedbackGetResponse getFeedbackById(Long feedbackId);

    /**
     * Cập nhật trạng thái hoặc thêm câu trả lời (response) cho phản hồi.
     *
     * @param feedbackId ID của phản hồi cần cập nhật
     * @param apiRequest đối tượng chứa dữ liệu cập nhật
     * @return ID của phản hồi vừa được cập nhật
     */
    Long updateFeedback(Long feedbackId, FeedbackUpdateRequest apiRequest);

    /**
     * Tạo mới một phản hồi từ cư dân.
     *
     * @param apiRequest đối tượng chứa dữ liệu tạo phản hồi
     * @return ID của phản hồi vừa tạo
     */
    Long createFeedback(FeedbackCreateRequest apiRequest);

    /**
     * Xóa một phản hồi (thường chỉ dành cho admin hoặc khi chưa xử lý).
     *
     * @param feedbackId ID của phản hồi cần xóa
     * @return Void
     */
    Void deleteFeedback(Long feedbackId);
}
