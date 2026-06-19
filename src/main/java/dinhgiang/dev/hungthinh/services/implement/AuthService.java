package dinhgiang.dev.hungthinh.services.implement;

import dinhgiang.dev.hungthinh.components.AuditLogContext;
import dinhgiang.dev.hungthinh.exceptions.UserMessageException;
import dinhgiang.dev.hungthinh.models.dtos.auths.LoginRequest;
import dinhgiang.dev.hungthinh.models.dtos.auths.LoginResponse;
import dinhgiang.dev.hungthinh.models.entities.enums.UserRole;
import dinhgiang.dev.hungthinh.services.interfaces.IAuthService;
import lombok.RequiredArgsConstructor;
import dinhgiang.dev.hungthinh.models.dtos.auths.LoginRequest;
import dinhgiang.dev.hungthinh.models.dtos.auths.LoginResponse;
import dinhgiang.dev.hungthinh.models.entities.enums.UserRole;
import dinhgiang.dev.hungthinh.services.interfaces.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

/**
 * Lớp dịch vụ xử lý xác thực (Authentication).
 * Đăng nhập, kiểm tra thông tin tài khoản và sinh JWT.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;

    public LoginResponse login(LoginRequest apiRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        apiRequest.getUsername(),
                        apiRequest.getPassword()
                )
        );

        UserRole role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", ""))
                .map(UserRole::valueOf)
                .orElseThrow(() -> new UserMessageException("Không có role"));

        String token = jwtService.generateJwtToken(apiRequest.getUsername(), role);
        auditLogService.logAs(apiRequest.getUsername(), role.name(), "LOGIN", "AUTH", null, apiRequest.getUsername(), "Đăng nhập hệ thống");
        AuditLogContext.markLogged();
        return new LoginResponse(apiRequest.getUsername(), token, role);
    }
}
