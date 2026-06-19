package dinhgiang.dev.hungthinh.controllers;

import dinhgiang.dev.hungthinh.models.dtos.devices.DeviceCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.devices.DeviceGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.devices.DeviceUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.enums.DeviceStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.DeviceType;
import dinhgiang.dev.hungthinh.models.entities.global.ApiResponse;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import dinhgiang.dev.hungthinh.services.interfaces.IDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
public class DeviceController extends BaseController {
    private final IDeviceService deviceService;

    @Operation(summary = "Lấy danh sách thiết bị")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<PageResponse<DeviceGetResponse>>> getAllDevice(
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng phần tử trong một trang") int size,
            @Parameter(description = "Trạng thái thiết bị: ACTIVE, INACTIVE, UNDER_MAINTENANCE,BROKEN") @RequestParam(required = false) DeviceStatus deviceStatus,
            @Parameter(description = "Loại thiết bị: COMMON, APARTMENT") @RequestParam(required = false) DeviceType deviceType,
            @Parameter(description = "Sẵp xếp theo id") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Theo thứ tự asc") @RequestParam(defaultValue = "asc") String direction,
            @Parameter(description = "Từ khóa tìm kiếm (Theo deviceName, location, maintenanceCycleDay)") @RequestParam(name = "keyword", required = false) String keyword,
            @Parameter(description = "Lọc theo căn hộ") @RequestParam(required = false) Long apartmentId
    ) {
        return success(deviceService.getAllDevices(page, size, deviceStatus, deviceType, sortBy, direction, keyword, apartmentId), "Lấy danh sách thiết bị thành công");
    }

    @Operation(summary = "Lấy thiết bị theo ID")
    @GetMapping("/{deviceId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<DeviceGetResponse>> getDeviceById(@PathVariable Long deviceId) {
        return success(deviceService.getDeviceById(deviceId), "Lấy thiết bị thành công");
    }

    @Operation(summary = "Tạo mới thiết bị")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> createDevice(@RequestBody DeviceCreateRequest apiRequest) {
        return success(deviceService.createDevice(apiRequest), "Tạo mới thiết bị thành công");
    }

    @Operation(summary = "Cập nhật thiết bị")
    @PatchMapping("/{deviceId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<Long>> updateDevice(
            @PathVariable Long deviceId,
            @RequestBody DeviceUpdateRequest apiRequest
    ) {
        return success(deviceService.updateDevice(deviceId, apiRequest), "Cập nhật thiết bị thành công");
    }

    @Operation(summary = "Xóa thiết bị")
    @DeleteMapping("/{deviceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDevice(@PathVariable Long deviceId) {
        return success(deviceService.deleteDevice(deviceId), "Xóa thiết bị thành công");
    }
}
