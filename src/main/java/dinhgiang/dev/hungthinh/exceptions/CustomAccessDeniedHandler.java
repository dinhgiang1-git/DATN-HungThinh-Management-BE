package dinhgiang.dev.hungthinh.exceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import dinhgiang.dev.hungthinh.models.entities.global.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        ApiResponse<?> body = ApiResponse.error("Bạn không có quyền hạn: " + request.getRequestURI());

        new ObjectMapper().writeValue(response.getOutputStream(), body);
    }
}
