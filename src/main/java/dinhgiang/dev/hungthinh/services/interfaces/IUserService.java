package dinhgiang.dev.hungthinh.services.interfaces;

import dinhgiang.dev.hungthinh.models.dtos.users.UserCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.users.UserGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.users.UserUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.enums.UserRole;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;

/**
 * Interface định nghĩa các nghiệp vụ liên quan đến quản lý Người dùng (User).
 * Dùng cho các tài khoản nội bộ ban quản lý: Admin, Kỹ thuật viên (Technician).
 * Không dùng cho cư dân (đã có ResidentService).
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface IUserService {

    /**
     * Lấy danh sách người dùng (Admin, Technician) có phân trang và lọc.
     *
     * @param page      trang hiện tại
     * @param size      số lượng phần tử trên mỗi trang
     * @param userRole  vai trò người dùng cần lọc
     * @param sortBy    trường cần sắp xếp
     * @param direction hướng sắp xếp
     * @param keyword   từ khóa tìm kiếm (tên, username, sđt)
     * @return PageResponse chứa danh sách UserGetResponse
     */
    PageResponse<UserGetResponse> getAllUsers(int page, int size, UserRole userRole, String sortBy, String direction, String keyword);

    /**
     * Lấy thông tin chi tiết một người dùng theo username.
     *
     * @param username Tên đăng nhập
     * @return UserGetResponse chứa thông tin người dùng
     */
    UserGetResponse getUserByUsername(String username);

    /**
     * Tạo tài khoản người dùng mới (Admin, Technician).
     *
     * @param apiRequest đối tượng chứa dữ liệu tạo người dùng
     * @return ID của người dùng vừa tạo
     */
    Long createUser(UserCreateRequest apiRequest);

    /**
     * Cập nhật thông tin cá nhân hoặc vai trò của người dùng.
     *
     * @param userId     ID của người dùng cần cập nhật
     * @param apiRequest đối tượng chứa dữ liệu cập nhật
     * @return ID của người dùng vừa được cập nhật
     */
    Long updateUser(Long userId, UserUpdateRequest apiRequest);

    /**
     * Xóa tài khoản người dùng khỏi hệ thống.
     *
     * @param userId ID của người dùng cần xóa
     * @return Void
     */
    Void deleteUser(Long userId);
}
