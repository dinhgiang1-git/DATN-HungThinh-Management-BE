package dinhgiang.dev.hungthinh.models.dtos.users;

import dinhgiang.dev.hungthinh.models.entities.enums.UserRole;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserGetResponse {
    private Long id;
    private String username;
    private String fullName;
    private String phoneNumber;
    private String email;
    private UserRole userRole;

}
