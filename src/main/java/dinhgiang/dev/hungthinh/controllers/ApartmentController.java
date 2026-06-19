package dinhgiang.dev.hungthinh.controllers;

import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentStatisticsResponse;
import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.enums.ApartmentStatus;
import dinhgiang.dev.hungthinh.models.entities.global.ApiResponse;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import dinhgiang.dev.hungthinh.services.interfaces.IApartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/apartments")
@RequiredArgsConstructor
public class ApartmentController extends BaseController{
    public final IApartmentService apartmentService;

    @Operation(summary = "Lấy danh sách căn hộ")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<PageResponse<ApartmentGetResponse>>> getAllApartments(
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng item trong một trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trạng thái căn hộ: OCCUPIED, VACANT, UNDER_MAINTENANCE") @RequestParam(required = false) ApartmentStatus apartmentStatus,
            @Parameter(description = "Sắp xếp theo id") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Theo thứ tự asc") @RequestParam(defaultValue = "asc") String direction,
            @Parameter(description = "Tìm kiếm theo từ khóa") @RequestParam(name = "keyword", required = false) String keyword,
            @Parameter(description = "Lọc theo khu chung cư/dự án") @RequestParam(required = false) String complexName,
            @Parameter(description = "Lọc theo Block") @RequestParam(required = false) String block,
            @Parameter(description = "Lọc theo Tầng") @RequestParam(required = false) Integer floor,
            @Parameter(description = "Lọc căn hộ có thiết bị") @RequestParam(required = false) Boolean hasDevices
    ) {
        return success(apartmentService.getAllApartment(page, size, apartmentStatus, sortBy, direction, keyword, complexName, block, floor, hasDevices), "Lấy danh sách căn hộ thành công");
    }

    @Operation(summary = "Thống kê căn hộ theo khu, tòa, tầng hoặc căn hộ")
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ApartmentStatisticsResponse>> getApartmentStatistics(
            @Parameter(description = "Cấp tổng hợp: COMPLEX, BLOCK, FLOOR, APARTMENT") @RequestParam(defaultValue = "COMPLEX") String groupBy,
            @Parameter(description = "Lọc theo khu chung cư/dự án") @RequestParam(required = false) String complexName,
            @Parameter(description = "Lọc theo Block") @RequestParam(required = false) String block,
            @Parameter(description = "Lọc theo Tầng") @RequestParam(required = false) Integer floor
    ) {
        return success(apartmentService.getApartmentStatistics(groupBy, complexName, block, floor), "Lấy thống kê căn hộ thành công");
    }

    @Operation(summary = "Lấy căn hộ theo id")
    @GetMapping("/{apartmentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<ApartmentGetResponse>> getApartmentById(@PathVariable Long apartmentId) {
        return success(apartmentService.getApartmentById(apartmentId), "Lấy căn hộ thành công");
    }

    @GetMapping("/resident/{residentId}")
    @Operation(summary = "Lấy căn hộ theo ResidentID")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<ApartmentGetResponse>> getApartmentByResident(@PathVariable Long residentId) {
        return success(apartmentService.getApartmentByResident(residentId), "Lấy căn hộ thành công");
    }

    @Operation(summary = "Tạo mới căn hộ")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> createApartment(@RequestBody ApartmentCreateRequest apiRequest) {
        return success(apartmentService.createApartment(apiRequest), "Tạo mới căn hộ thành công");
    }

    @Operation(summary = "Cập nhật căn hộ (PATCH)")
    @PatchMapping("/{apartmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> updateApartment(@PathVariable Long apartmentId, @RequestBody ApartmentUpdateRequest apiRequest) {
        return success(apartmentService.updateApartment(apartmentId, apiRequest), "Cập nhật căn hộ thành công");
    }

    @Operation(summary = "Xóa căn hộ")
    @DeleteMapping("/{apartmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteApartment(@PathVariable Long apartmentId) {
        return success(apartmentService.deleteApartment(apartmentId), "Xóa căn hộ thành công");
    }
}
