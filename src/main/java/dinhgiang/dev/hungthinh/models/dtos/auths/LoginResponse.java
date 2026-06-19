package dinhgiang.dev.hungthinh.models.dtos.auths;

import dinhgiang.dev.hungthinh.models.entities.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {
    private String username;
    private String token;
    private UserRole role;
}
