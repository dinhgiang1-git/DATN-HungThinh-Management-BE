package dinhgiang.dev.hungthinh.services.interfaces;

import dinhgiang.dev.hungthinh.models.dtos.notifications.NotificationCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.notifications.NotificationGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.notifications.NotificationUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.enums.TargetType;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import dinhgiang.dev.hungthinh.models.dtos.notifications.ReceiverGetResponse;

/**
 * Interface định nghĩa các nghiệp vụ liên quan đến Thông báo chung (Notification).
 * Cho phép tạo thông báo đẩy cho toàn bộ tòa nhà, theo block hoặc cho căn hộ cụ thể.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface INotificationService {

    /**
     * Lấy danh sách thông báo chung của hệ thống (dành cho Admin).
     *
     * @param page       trang hiện tại
     * @param size       số lượng phần tử trên mỗi trang
     * @param targetType phạm vi gửi (ALL, BLOCK, APARTMENT)
     * @param sortBy     trường cần sắp xếp
     * @param direction  hướng sắp xếp
     * @param keyword    từ khóa tìm kiếm (tiêu đề, nội dung)
     * @return PageResponse chứa danh sách NotificationGetResponse
     */
    PageResponse<NotificationGetResponse> getAllNotifications(int page, int size, TargetType targetType, String sortBy, String direction, String keyword);

    /**
     * Lấy danh sách thông báo mà một cư dân cụ thể nhận được (dành cho Resident).
     *
     * @param residentId ID của cư dân
     * @param page       trang hiện tại
     * @param size       số lượng phần tử trên mỗi trang
     * @param sortBy     trường cần sắp xếp
     * @param direction  hướng sắp xếp
     * @param keyword    từ khóa tìm kiếm
     * @return PageResponse chứa danh sách NotificationGetResponse
     */
    PageResponse<NotificationGetResponse> getNotificationsByResident(Long residentId, int page, int size, String sortBy, String direction, String keyword);

    /**
     * Lấy thông tin chi tiết một thông báo.
     *
     * @param notificationId ID của thông báo
     * @return NotificationGetResponse chứa thông tin chi tiết
     */
    NotificationGetResponse getNotificationById(Long notificationId);

    /**
     * Lấy danh sách cư dân (người nhận) của một thông báo, cùng trạng thái đã đọc hay chưa.
     *
     * @param notificationId ID của thông báo
     * @param page           trang hiện tại
     * @param size           số phần tử trên mỗi trang
     * @param keyword        từ khóa tìm kiếm tên cư dân
     * @param isRead         lọc theo trạng thái đã đọc (true/false)
     * @return PageResponse chứa danh sách ReceiverGetResponse
     */
    PageResponse<ReceiverGetResponse> getReceiversByNotification(Long notificationId, int page, int size, String keyword, Boolean isRead);

    /**
     * Tạo mới và gửi thông báo tới các đối tượng được chỉ định.
     *
     * @param apiRequest đối tượng chứa dữ liệu thông báo
     * @return ID của thông báo vừa tạo
     */
    Long createNotification(NotificationCreateRequest apiRequest);

    /**
     * Cập nhật nội dung thông báo.
     *
     * @param notificationId ID của thông báo cần cập nhật
     * @param apiRequest     đối tượng chứa dữ liệu cập nhật
     * @return ID của thông báo vừa được cập nhật
     */
    Long updateNotification(Long notificationId, NotificationUpdateRequest apiRequest);

    /**
     * Xóa thông báo (có thể dẫn đến xóa các notification_receivers tương ứng).
     *
     * @param notificationId ID của thông báo cần xóa
     * @return Void
     */
    Void deleteNotification(Long notificationId);

    /**
     * Đánh dấu một thông báo đã được đọc bởi một cư dân.
     *
     * @param notificationId ID của thông báo
     * @param residentId     ID của cư dân
     */
    void markAsRead(Long notificationId, Long residentId);
}
