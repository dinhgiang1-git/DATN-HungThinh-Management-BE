package dinhgiang.dev.hungthinh.models.dtos.users;

import dinhgiang.dev.hungthinh.models.entities.enums.UserRole;
import lombok.Data;

@Data
public class UserUpdateRequest {
    private String password;
    private UserRole userRole;
    private String fullName;
    private String phoneNumber;
    private String email;
}
