package dinhgiang.dev.hungthinh.controllers;

import dinhgiang.dev.hungthinh.models.dtos.vehicles.VehicleCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.vehicles.VehicleGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.vehicles.VehicleUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.global.ApiResponse;
import dinhgiang.dev.hungthinh.services.implement.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController extends BaseController {

    private final VehicleService vehicleService;

    @Operation(summary = "Lấy tất cả xe")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<VehicleGetResponse>>> getAllVehicles() {
        return success(vehicleService.getAllVehicles(), "Lấy danh sách xe thành công");
    }

    @Operation(summary = "Lấy danh sách xe theo căn hộ")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<List<VehicleGetResponse>>> getVehiclesByApartment(
            @RequestParam("apartmentId") Long apartmentId
    ) {
        return success(vehicleService.getVehiclesByApartmentId(apartmentId), "Lấy danh sách xe thành công");
    }

    @Operation(summary = "Lấy xe theo ID")
    @GetMapping("/{vehicleId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<VehicleGetResponse>> getVehicleById(@PathVariable Long vehicleId) {
        return success(vehicleService.getVehicleById(vehicleId), "Lấy xe thành công");
    }

    @Operation(summary = "Thêm xe mới")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<Long>> createVehicle(@RequestBody VehicleCreateRequest request) {
        return success(vehicleService.createVehicle(request), "Thêm xe thành công");
    }

    @Operation(summary = "Cập nhật xe")
    @PatchMapping("/{vehicleId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<Long>> updateVehicle(
            @PathVariable Long vehicleId,
            @RequestBody VehicleUpdateRequest request
    ) {
        return success(vehicleService.updateVehicle(vehicleId, request), "Cập nhật xe thành công");
    }

    @Operation(summary = "Xóa xe")
    @DeleteMapping("/{vehicleId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(@PathVariable Long vehicleId) {
        return success(vehicleService.deleteVehicle(vehicleId), "Xóa xe thành công");
    }

    @Operation(summary = "Tính phí gửi xe cho căn hộ")
    @GetMapping("/parking-fee")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BigDecimal>> calculateParkingFee(
            @RequestParam Long apartmentId,
            @RequestParam(defaultValue = "0") BigDecimal motorbikeFee,
            @RequestParam(defaultValue = "0") BigDecimal carFee,
            @RequestParam(defaultValue = "0") BigDecimal bicycleFee,
            @RequestParam(defaultValue = "0") BigDecimal electricMotorbikeFee
    ) {
        return success(vehicleService.calculateParkingFee(apartmentId, motorbikeFee, carFee, bicycleFee, electricMotorbikeFee), "Tính phí gửi xe thành công");
    }
}
