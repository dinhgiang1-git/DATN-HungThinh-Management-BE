package dinhgiang.dev.hungthinh.components;

import dinhgiang.dev.hungthinh.services.implement.AuditLogService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestAuditFilter extends OncePerRequestFilter {

    private final AuditLogService auditLogService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } finally {
            try {
                writeRequestAuditIfNeeded(request, response);
            } catch (Exception e) {
                log.warn("Request audit failed: {}", e.getMessage());
            } finally {
                AuditLogContext.clear();
            }
        }
    }

    private void writeRequestAuditIfNeeded(HttpServletRequest request, HttpServletResponse response) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        int status = response.getStatus();

        if (shouldIgnore(path, method) || AuditLogContext.alreadyLogged()) {
            return;
        }

        boolean writeOperation = !method.equalsIgnoreCase("GET") && !method.equalsIgnoreCase("OPTIONS");
        boolean failedApiCall = status >= 400 && path.startsWith("/api/");
        if (!writeOperation && !failedApiCall) {
            return;
        }

        String action = status >= 400 ? "FAILED" : resolveAction(method, path);
        String entityType = resolveEntityType(path);
        Long entityId = extractEntityId(path);
        String details = buildDetails(action, method, path, status);

        auditLogService.log(
                action,
                entityType,
                entityId,
                null,
                details,
                method.toUpperCase(Locale.ROOT),
                path,
                clientIp(request),
                status
        );
    }

    private boolean shouldIgnore(String path, String method) {
        return path.startsWith("/api/v1/audit-logs")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/notifications/stream")
                || path.startsWith("/ws")
                || method.equalsIgnoreCase("OPTIONS");
    }

    private String resolveAction(String method, String path) {
        if (path.contains("/auth/login")) return "LOGIN";
        if (path.contains("/payment/momo")) return "PAYMENT_CALLBACK";
        return switch (method.toUpperCase(Locale.ROOT)) {
            case "POST" -> "CREATE";
            case "PUT", "PATCH" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> "ACCESS";
        };
    }

    private String resolveEntityType(String path) {
        String normalized = path.toLowerCase(Locale.ROOT);
        if (normalized.contains("/auth")) return "AUTH";
        if (normalized.contains("/users")) return "USER";
        if (normalized.contains("/residents")) return "RESIDENT";
        if (normalized.contains("/apartments")) return "APARTMENT";
        if (normalized.contains("/devices")) return "DEVICE";
        if (normalized.contains("/notifications")) return "NOTIFICATION";
        if (normalized.contains("/feedbacks")) return "FEEDBACK";
        if (normalized.contains("/invoices")) return "INVOICE";
        if (normalized.contains("/meter-readings")) return "METER_READING";
        if (normalized.contains("/contracts")) return "CONTRACT";
        if (normalized.contains("/maintenances")) return "MAINTENANCE";
        if (normalized.contains("/vehicles")) return "VEHICLE";
        if (normalized.contains("/payment")) return "PAYMENT";
        if (normalized.contains("/table-fees")) return "TABLE_FEE";
        if (normalized.contains("/table-electric-tiers")) return "ELECTRIC_TIER";
        if (normalized.contains("/system-notifications")) return "SYSTEM_NOTIFICATION";
        return "SYSTEM";
    }

    private Long extractEntityId(String path) {
        String[] parts = path.split("/");
        for (int i = parts.length - 1; i >= 0; i--) {
            try {
                return Long.parseLong(parts[i]);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private String buildDetails(String action, String method, String path, int status) {
        if ("FAILED".equals(action)) {
            return "Yêu cầu " + method + " " + path + " thất bại với mã " + status;
        }
        if ("LOGIN".equals(action)) {
            return "Đăng nhập hệ thống";
        }
        return "Yêu cầu " + method + " " + path + " hoàn tất với mã " + status;
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
