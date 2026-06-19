package dinhgiang.dev.hungthinh.services.implement;

import dinhgiang.dev.hungthinh.models.dtos.auditlogs.AuditLogGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.auditlogs.AuditLogSummaryResponse;
import dinhgiang.dev.hungthinh.models.entities.bases.AuditLog;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import dinhgiang.dev.hungthinh.repositories.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lớp dịch vụ quản lý nhật ký hệ thống (Audit Log).
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Ghi 1 dòng audit log
     */
    public void log(String action, String entityType, Long entityId, String entityName, String details) {
        log(action, entityType, entityId, entityName, details, null, null, null, null);
    }

    public void log(
            String action,
            String entityType,
            Long entityId,
            String entityName,
            String details,
            String httpMethod,
            String requestPath,
            String ipAddress,
            Integer statusCode
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null && auth.isAuthenticated() ? auth.getName() : "SYSTEM";
        String role = auth != null && auth.isAuthenticated()
                ? auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(a -> a.startsWith("ROLE_"))
                    .map(a -> a.replace("ROLE_", ""))
                    .findFirst().orElse("UNKNOWN")
                : "SYSTEM";

        save(action, entityType, entityId, entityName, details, httpMethod, requestPath, ipAddress, statusCode, username, role);
    }

    public void logAs(
            String username,
            String role,
            String action,
            String entityType,
            Long entityId,
            String entityName,
            String details
    ) {
        save(action, entityType, entityId, entityName, details, null, null, null, null, username, role);
    }

    private void save(
            String action,
            String entityType,
            Long entityId,
            String entityName,
            String details,
            String httpMethod,
            String requestPath,
            String ipAddress,
            Integer statusCode,
            String username,
            String role
    ) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .entityName(entityName)
                .performedBy(username)
                .performerRole(role)
                .details(details)
                .httpMethod(httpMethod)
                .requestPath(requestPath)
                .ipAddress(ipAddress)
                .statusCode(statusCode)
                .build();

        auditLogRepository.save(auditLog);
    }

    /**
     * Lấy danh sách audit log với phân trang + lọc
     */
    public PageResponse<AuditLogGetResponse> getAll(
            int page, int size,
            String entityType, String action,
            String performedBy,
            LocalDate from, LocalDate to,
            String keyword
    ) {
        Sort sort = Sort.by("createdAt").descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Specification<AuditLog> spec = Specification.where(null);

        if (entityType != null && !entityType.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("entityType"), entityType));
        }
        if (action != null && !action.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("action"), action));
        }
        if (performedBy != null && !performedBy.isBlank()) {
            spec = spec.and((root, q, cb) -> cb.like(cb.lower(root.get("performedBy")), "%" + performedBy.toLowerCase() + "%"));
        }
        if (from != null) {
            LocalDateTime fromStart = from.atStartOfDay();
            spec = spec.and((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), fromStart));
        }
        if (to != null) {
            LocalDateTime toEnd = to.plusDays(1).atStartOfDay();
            spec = spec.and((root, q, cb) -> cb.lessThan(root.get("createdAt"), toEnd));
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.toLowerCase() + "%";
            spec = spec.and((root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("entityName")), kw),
                    cb.like(cb.lower(root.get("details")), kw),
                    cb.like(cb.lower(root.get("requestPath")), kw),
                    cb.like(cb.lower(root.get("ipAddress")), kw)
            ));
        }

        Page<AuditLog> logs = auditLogRepository.findAll(spec, pageRequest);

        List<AuditLogGetResponse> content = logs.getContent().stream()
                .map(log -> AuditLogGetResponse.builder()
                        .id(log.getId())
                        .action(log.getAction())
                        .entityType(log.getEntityType())
                        .entityId(log.getEntityId())
                        .entityName(log.getEntityName())
                        .performedBy(log.getPerformedBy())
                        .performerRole(log.getPerformerRole())
                        .details(log.getDetails())
                        .httpMethod(log.getHttpMethod())
                        .requestPath(log.getRequestPath())
                        .ipAddress(log.getIpAddress())
                        .statusCode(log.getStatusCode())
                        .createdAt(log.getCreatedAt())
                        .build())
                .toList();

        return new PageResponse<>(
                content,
                logs.getNumber() + 1,
                logs.getSize(),
                logs.getTotalElements(),
                logs.getTotalPages()
        );
    }

    public AuditLogSummaryResponse getSummary(LocalDate from, LocalDate to) {
        LocalDateTime fromTime = from != null ? from.atStartOfDay() : LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime toTime = to != null ? to.plusDays(1).atStartOfDay() : LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();

        return AuditLogSummaryResponse.builder()
                .total(auditLogRepository.countByCreatedAtBetween(fromTime, toTime))
                .todayTotal(auditLogRepository.countByCreatedAtBetween(todayStart, tomorrowStart))
                .failedTotal(auditLogRepository.countByActionAndCreatedAtBetween("FAILED", fromTime, toTime))
                .activeUsers(auditLogRepository.countDistinctPerformedByBetween(fromTime, toTime))
                .byAction(toCountMap(auditLogRepository.countByActionBetween(fromTime, toTime)))
                .byEntityType(toCountMap(auditLogRepository.countByEntityTypeBetween(fromTime, toTime)))
                .build();
    }

    private Map<String, Long> toCountMap(List<Object[]> rows) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            result.put((String) row[0], (Long) row[1]);
        }
        return result;
    }
}
