package dinhgiang.dev.hungthinh.models.dtos.users;

import dinhgiang.dev.hungthinh.models.entities.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserCreateRequest {
    @NotBlank(message = "Tên người dùng không được để trống")
    private String userName;
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
    @NotNull(message = "Vai trò người dùng không được để trống")
    private UserRole userRole;
    @NotBlank(message = "Tên không được để trống")
    private String fullName;
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)(3[2-9]|5[6-9]|7[0|6-9]|8[0-9]|9[0-9])[0-9]{7}$",
            message = "Số điện thoại không hợp lệ")
    private String phoneNumber;
    @Email(message = "Email không hợp lệ")
    private String email;

}
