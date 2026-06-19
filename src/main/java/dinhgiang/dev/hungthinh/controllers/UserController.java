package dinhgiang.dev.hungthinh.controllers;

import dinhgiang.dev.hungthinh.models.dtos.users.UserCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.users.UserGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.users.UserUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.enums.UserRole;
import dinhgiang.dev.hungthinh.models.entities.global.ApiResponse;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import dinhgiang.dev.hungthinh.services.interfaces.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController extends BaseController {
    private final IUserService userService;

    @Operation(summary = "Lấy danh sách người dùng")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<PageResponse<UserGetResponse>>> getAllUser(
            @Parameter(description = "Số trang") @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Số lượng item trong một trang") @RequestParam(name = "size", defaultValue = "10") int size,
            @Parameter(description = "Vai trò: ADMIN, RESIDENT, TECHNICIAN") @RequestParam(name = "userRole", required = false) UserRole userRole,
            @Parameter(description = "Sẵp xếp theo id") @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @Parameter(description = "Theo thứ tự asc") @RequestParam(name = "direction", defaultValue = "asc") String direction,
            @Parameter(description = "Từ khóa tìm kiếm (Theo FullName, Phone, Email)") @RequestParam(name = "keyword", required = false) String keyword
    ) {
        return success(userService.getAllUsers(page, size, userRole, sortBy, direction, keyword), "Lấy danh sách người dùng thành công");
    }

    @Operation(summary = "Lấy người dùng theo username")
    @GetMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<UserGetResponse>> getUserByUsername(@PathVariable String username) {
        return success(userService.getUserByUsername(username), "Lấy người dùng thành công");
    }

    @Operation(summary = "Tạo mới người dùng")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> createUser(@Valid @RequestBody UserCreateRequest apiRequest) {
        return success(userService.createUser(apiRequest), "Tạo mới người dùng thành công");
    }

    @Operation(summary = "Chỉnh sửa người dùng (PATCH)")
    @PatchMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<Long>> updateUser(@PathVariable Long userId, @RequestBody UserUpdateRequest apiRequest) {
        return success(userService.updateUser(userId, apiRequest), "Cập nhật người dùng thành công");
    }

    @Operation(summary = "Xóa người dùng")
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        return success(userService.deleteUser(userId), "Xóa người dùng thành công");
    }

}
