package dinhgiang.dev.hungthinh.services.interfaces;

import dinhgiang.dev.hungthinh.models.dtos.devices.DeviceCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.devices.DeviceGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.devices.DeviceUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.enums.DeviceStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.DeviceType;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;

/**
 * Interface định nghĩa các nghiệp vụ liên quan đến quản lý Thiết bị.
 * Cho phép xem, thêm, sửa, xóa các thiết bị dùng chung hoặc thiết bị thuộc căn hộ.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface IDeviceService {

    /**
     * Lấy danh sách thiết bị có phân trang và lọc theo tiêu chí.
     *
     * @param page         trang hiện tại (bắt đầu từ 1)
     * @param size         số lượng phần tử trên mỗi trang
     * @param deviceStatus trạng thái thiết bị cần lọc
     * @param deviceType   loại thiết bị (dùng chung / căn hộ)
     * @param sortBy       trường cần sắp xếp
     * @param direction    hướng sắp xếp (ASC/DESC)
     * @param keyword      từ khóa tìm kiếm (tên, vị trí)
     * @param apartmentId  ID căn hộ (nếu lọc theo thiết bị căn hộ)
     * @return PageResponse chứa danh sách DeviceGetResponse
     */
    PageResponse<DeviceGetResponse> getAllDevices(int page, int size, DeviceStatus deviceStatus, DeviceType deviceType, String sortBy, String direction, String keyword, Long apartmentId);

    /**
     * Lấy thông tin chi tiết một thiết bị.
     *
     * @param deviceId ID của thiết bị
     * @return DeviceGetResponse chứa thông tin thiết bị
     */
    DeviceGetResponse getDeviceById(Long deviceId);

    /**
     * Thêm mới một thiết bị vào hệ thống.
     *
     * @param apiRequest đối tượng chứa dữ liệu tạo thiết bị
     * @return ID của thiết bị vừa tạo
     */
    Long createDevice(DeviceCreateRequest apiRequest);

    /**
     * Cập nhật thông tin thiết bị hiện có.
     *
     * @param deviceId   ID của thiết bị cần cập nhật
     * @param apiRequest đối tượng chứa dữ liệu cập nhật
     * @return ID của thiết bị vừa được cập nhật
     */
    Long updateDevice(Long deviceId, DeviceUpdateRequest apiRequest);

    /**
     * Xóa một thiết bị khỏi hệ thống.
     *
     * @param deviceId ID của thiết bị cần xóa
     * @return Void
     */
    Void deleteDevice(Long deviceId);
}
