package dinhgiang.dev.hungthinh.services.interfaces;


import dinhgiang.dev.hungthinh.models.dtos.residents.ResidentCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.residents.ResidentGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.residents.ResidentUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.enums.RelationshipType;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;

/**
 * Interface định nghĩa các nghiệp vụ liên quan đến quản lý Cư dân.
 * Bao gồm xem, thêm, sửa, xóa thông tin cư dân và gán vào căn hộ.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface IResidentService {

    /**
     * Lấy danh sách cư dân có phân trang và lọc theo nhiều tiêu chí.
     *
     * @param page             trang hiện tại
     * @param size             số lượng phần tử trên mỗi trang
     * @param relationshipType vai trò của cư dân trong hộ gia đình
     * @param hasApartment     lọc xem cư dân đã được gán vào căn hộ nào chưa
     * @param sortBy           trường cần sắp xếp
     * @param direction        hướng sắp xếp
     * @param keyword          từ khóa tìm kiếm (tên, số điện thoại, CMND/CCCD)
     * @return PageResponse chứa danh sách ResidentGetResponse
     */
    PageResponse<ResidentGetResponse> getAllResident(int page, int size, RelationshipType relationshipType, Boolean hasApartment, String sortBy, String direction, String keyword);

    /**
     * Lấy thông tin chi tiết của một cư dân theo ID.
     *
     * @param residentId ID của cư dân
     * @return ResidentGetResponse chứa thông tin chi tiết
     */
    ResidentGetResponse getResidentById(Long residentId);

    /**
     * Lấy thông tin cư dân theo username (tên đăng nhập).
     *
     * @param username Tên đăng nhập của cư dân
     * @return ResidentGetResponse chứa thông tin cư dân
     */
    ResidentGetResponse getResidentByUsername(String username);

    /**
     * Đăng ký/Tạo mới thông tin cư dân vào hệ thống.
     * Cư dân sẽ được cấp tài khoản để đăng nhập (UserRole.RESIDENT).
     *
     * @param apiRequest đối tượng chứa thông tin cư dân
     * @return ID của cư dân vừa tạo
     */
    Long createResident(ResidentCreateRequest apiRequest);

    /**
     * Cập nhật thông tin cá nhân của cư dân.
     *
     * @param residentId ID của cư dân cần cập nhật
     * @param apiRequest đối tượng chứa dữ liệu cập nhật
     * @return ID của cư dân vừa được cập nhật
     */
    Long updateResident(Long residentId, ResidentUpdateRequest apiRequest);

    /**
     * Xóa thông tin cư dân khỏi hệ thống.
     *
     * @param residentId ID của cư dân cần xóa
     * @return Void
     */
    Void deleteResident(Long residentId);
}
