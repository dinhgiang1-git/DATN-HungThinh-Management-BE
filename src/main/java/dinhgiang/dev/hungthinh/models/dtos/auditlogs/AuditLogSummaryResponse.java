package dinhgiang.dev.hungthinh.models.dtos.auditlogs;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class AuditLogSummaryResponse {
    private long total;
    private long todayTotal;
    private long failedTotal;
    private long activeUsers;
    private Map<String, Long> byAction;
    private Map<String, Long> byEntityType;
}
