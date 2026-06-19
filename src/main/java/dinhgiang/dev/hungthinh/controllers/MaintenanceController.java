package dinhgiang.dev.hungthinh.controllers;

import dinhgiang.dev.hungthinh.models.dtos.maintenances.MaintenanceCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.maintenances.MaintenanceGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.maintenances.MaintenanceUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.enums.MaintenanceStatus;
import dinhgiang.dev.hungthinh.models.entities.global.ApiResponse;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import dinhgiang.dev.hungthinh.services.interfaces.IMaintenanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/maintenances")
@RequiredArgsConstructor
public class MaintenanceController extends BaseController{
    private final IMaintenanceService maintenanceService;

    @Operation(summary = "Lấy danh sách bảo trì")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<PageResponse<MaintenanceGetResponse>>> getAllMaintenances(
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng item trong một trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trạng thái bảo trì: SCHEDULED, COMPLETED, CANCELLED") @RequestParam(required = false)MaintenanceStatus maintenanceStatus,
            @Parameter(description = "Sẵp xếp theo id") String sortBy,
            @Parameter(description = "Theo thứ tự asc") @RequestParam(defaultValue = "asc") String direction,
            @Parameter(description = "Từ khóa tìm kiếm (Theo CompleteDay, StartedDay, Cost, Description)") @RequestParam(name = "keyword", required = false) String keyword
    ) {
        return success(maintenanceService.getAllMaintenance(page, size, maintenanceStatus, sortBy, direction, keyword), "Lấy danh sách bảo trì thành công");
    }

    @Operation(summary = "Lấy bảo trì theo ID")
    @GetMapping("/{maintenanceId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<MaintenanceGetResponse>> getMaintenanceById(@PathVariable Long maintenanceId) {
        return success(maintenanceService.getMaintenanceById(maintenanceId), "Lấy bảo trì thành công");
    }

    @Operation(summary = "Cập nhật bảo trì")
    @PatchMapping("/{maintenanceId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<Long>> updateMaintenance(
            @PathVariable Long maintenanceId,
            @RequestBody MaintenanceUpdateRequest apiRequest
    ) {
        return success(maintenanceService.updateMaintenance(maintenanceId, apiRequest), "Update thành công bảo trì");
    }

    @Operation(summary = "Tạo mới bảo trì")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<Long>> createMaintenance(@RequestBody MaintenanceCreateRequest apiRequest) {
        return success(maintenanceService.createMaintenance(apiRequest), "Tạo mới bảo trì thành công");
    }

    @DeleteMapping("/{maintenanceId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<Void>> deleteMaintenance(@PathVariable Long maintenanceId) {
        return success(maintenanceService.deleteMaintenance(maintenanceId), "Xóa bảo trì thành công");
    }
}
