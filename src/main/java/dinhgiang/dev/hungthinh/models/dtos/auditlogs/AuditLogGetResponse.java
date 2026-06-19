package dinhgiang.dev.hungthinh.models.dtos.auditlogs;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogGetResponse {
    private Long id;
    private String action;
    private String entityType;
    private Long entityId;
    private String entityName;
    private String performedBy;
    private String performerRole;
    private String details;
    private String httpMethod;
    private String requestPath;
    private String ipAddress;
    private Integer statusCode;
    private LocalDateTime createdAt;
}
