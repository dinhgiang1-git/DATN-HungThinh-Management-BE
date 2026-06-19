package dinhgiang.dev.hungthinh.services.interfaces;

import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentStatisticsResponse;
import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.enums.ApartmentStatus;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;

/**
 * Interface định nghĩa các nghiệp vụ liên quan đến quản lý Căn hộ.
 * Cung cấp các phương thức CRUD và tìm kiếm căn hộ.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface IApartmentService {

    /**
     * Lấy danh sách căn hộ có phân trang và lọc theo nhiều tiêu chí.
     *
     * @param page           trang hiện tại (bắt đầu từ 1)
     * @param size           số lượng phần tử trên mỗi trang
     * @param apartmentStatus trạng thái căn hộ để lọc (tuỳ chọn)
     * @param sortBy         trường cần sắp xếp
     * @param direction      hướng sắp xếp (ASC/DESC)
     * @param keyword        từ khóa tìm kiếm (theo mã căn hộ hoặc tên chủ hộ)
     * @param block          lọc theo block/tòa nhà
     * @param floor          lọc theo tầng
     * @param hasDevices     lọc theo trạng thái có thiết bị hay không
     * @return PageResponse chứa danh sách ApartmentGetResponse
     */
    PageResponse<ApartmentGetResponse> getAllApartment(int page, int size, ApartmentStatus apartmentStatus, String sortBy, String direction, String keyword, String complexName, String block, Integer floor, Boolean hasDevices);

    ApartmentStatisticsResponse getApartmentStatistics(String groupBy, String complexName, String block, Integer floor);

    /**
     * Lấy thông tin chi tiết một căn hộ theo ID.
     *
     * @param apartmentId ID của căn hộ cần tìm
     * @return ApartmentGetResponse chứa thông tin chi tiết căn hộ
     */
    ApartmentGetResponse getApartmentById(Long apartmentId);

    /**
     * Lấy thông tin căn hộ mà cư dân đang sinh sống.
     *
     * @param residentId ID của cư dân
     * @return ApartmentGetResponse chứa thông tin căn hộ
     */
    ApartmentGetResponse getApartmentByResident(Long residentId);

    /**
     * Tạo mới một căn hộ trong hệ thống.
     *
     * @param apiRequest đối tượng chứa dữ liệu tạo căn hộ
     * @return ID của căn hộ vừa được tạo
     */
    Long createApartment(ApartmentCreateRequest apiRequest);

    /**
     * Cập nhật thông tin căn hộ hiện có.
     *
     * @param apartmentId ID của căn hộ cần cập nhật
     * @param apiRequest  đối tượng chứa dữ liệu cập nhật
     * @return ID của căn hộ đã được cập nhật
     */
    Long updateApartment(Long apartmentId, ApartmentUpdateRequest apiRequest);

    /**
     * Xóa một căn hộ khỏi hệ thống.
     *
     * @param apartmentId ID của căn hộ cần xóa
     * @return Void
     */
    Void deleteApartment(Long apartmentId);

}
