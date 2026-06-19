package dinhgiang.dev.hungthinh.services.interfaces;

import dinhgiang.dev.hungthinh.models.dtos.maintenances.MaintenanceCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.maintenances.MaintenanceGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.maintenances.MaintenanceUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.enums.MaintenanceStatus;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;

/**
 * Interface định nghĩa các nghiệp vụ liên quan đến quản lý Bảo trì thiết bị/cơ sở vật chất.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface IMaintenanceService {

    /**
     * Lấy danh sách công việc bảo trì có phân trang và lọc.
     *
     * @param page              trang hiện tại
     * @param size              số lượng phần tử trên mỗi trang
     * @param maintenanceStatus trạng thái công việc bảo trì
     * @param sortBy            trường cần sắp xếp
     * @param direction         hướng sắp xếp
     * @param keyword           từ khóa tìm kiếm (mô tả công việc)
     * @return PageResponse chứa danh sách MaintenanceGetResponse
     */
    PageResponse<MaintenanceGetResponse> getAllMaintenance(int page, int size, MaintenanceStatus maintenanceStatus, String sortBy, String direction, String keyword);

    /**
     * Lấy thông tin chi tiết một công việc bảo trì.
     *
     * @param maintenanceId ID của công việc bảo trì
     * @return MaintenanceGetResponse chứa thông tin bảo trì
     */
    MaintenanceGetResponse getMaintenanceById(Long maintenanceId);

    /**
     * Cập nhật thông tin công việc bảo trì (cập nhật trạng thái, kỹ thuật viên phụ trách, ...).
     *
     * @param maintenanceId ID của công việc bảo trì cần cập nhật
     * @param apiReqeust    đối tượng chứa dữ liệu cập nhật
     * @return ID của công việc bảo trì vừa được cập nhật
     */
    Long updateMaintenance(Long maintenanceId, MaintenanceUpdateRequest apiReqeust);

    /**
     * Tạo mới một công việc bảo trì. Có thể liên kết với một Feedback hoặc Device.
     *
     * @param apiReqeust đối tượng chứa dữ liệu tạo công việc bảo trì
     * @return ID của công việc bảo trì vừa tạo
     */
    Long createMaintenance(MaintenanceCreateRequest apiReqeust);

    /**
     * Xóa một công việc bảo trì.
     *
     * @param maintenanceId ID của công việc bảo trì cần xóa
     * @return Void
     */
    Void deleteMaintenance(Long maintenanceId);
}
