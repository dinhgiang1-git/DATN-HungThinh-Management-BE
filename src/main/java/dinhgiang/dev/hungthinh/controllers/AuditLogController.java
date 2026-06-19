package dinhgiang.dev.hungthinh.controllers;

import dinhgiang.dev.hungthinh.models.dtos.auditlogs.AuditLogGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.auditlogs.AuditLogSummaryResponse;
import dinhgiang.dev.hungthinh.models.entities.global.ApiResponse;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import dinhgiang.dev.hungthinh.services.implement.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController extends BaseController {

    private final AuditLogService auditLogService;

    @Operation(summary = "Lấy tổng quan nhật ký hệ thống")
    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AuditLogSummaryResponse>> getAuditLogSummary(
            @Parameter(description = "Từ ngày") @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Đến ngày") @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return success(auditLogService.getSummary(from, to), "Lấy tổng quan nhật ký thành công");
    }

    @Operation(summary = "Lấy danh sách nhật ký hệ thống")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogGetResponse>>> getAuditLogs(
            @Parameter(description = "Số trang") @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Số phần tử/trang") @RequestParam(name = "size", defaultValue = "20") int size,
            @Parameter(description = "Loại đối tượng: INVOICE, CONTRACT, VEHICLE, APARTMENT, RESIDENT, USER, PAYMENT") @RequestParam(name = "entityType", required = false) String entityType,
            @Parameter(description = "Hành động: CREATE, UPDATE, DELETE, BATCH_CREATE") @RequestParam(name = "action", required = false) String action,
            @Parameter(description = "Người thực hiện") @RequestParam(name = "performedBy", required = false) String performedBy,
            @Parameter(description = "Từ ngày") @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "Đến ngày") @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam(name = "keyword", required = false) String keyword
    ) {
        return success(auditLogService.getAll(page, size, entityType, action, performedBy, from, to, keyword), "Lấy nhật ký thành công");
    }
}
