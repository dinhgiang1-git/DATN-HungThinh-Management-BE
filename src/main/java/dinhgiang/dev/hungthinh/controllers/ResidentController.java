package dinhgiang.dev.hungthinh.controllers;

import dinhgiang.dev.hungthinh.models.dtos.residents.ResidentCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.residents.ResidentGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.residents.ResidentUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.enums.RelationshipType;
import dinhgiang.dev.hungthinh.models.entities.global.ApiResponse;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import dinhgiang.dev.hungthinh.services.interfaces.IResidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/residents")
@RequiredArgsConstructor
public class ResidentController extends BaseController {
    private final IResidentService residentService;

    @Operation(summary = "Lấy danh sách cư dân")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<ResidentGetResponse>>> getAllResident(
            @Parameter(description = "Số trang") @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Số lượng item trong một trang") @RequestParam(name = "size", defaultValue = "10") int size,
            @Parameter(description = "Mối quan hệ: OWNER, SPOUSE, CHILD, PARENT, RELATIVE, TENANT, OTHER") @RequestParam(name = "relationshipType", required = false) RelationshipType relationshipType,
            @Parameter(description = "Cư dân đã có căn hộ hay chưa") @RequestParam(name = "hasApartment", required = false) Boolean hasApartment,
            @Parameter(description = "Sẵp xếp theo id") @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @Parameter(description = "Theo thứ tự asc") @RequestParam(name = "direction", defaultValue = "asc") String direction,
            @Parameter(description = "Từ khóa tìm kiếm (Theo FullName, Phone, Email)") @RequestParam(name = "keyword", required = false) String keyword
    ) {
        return success(residentService.getAllResident(page, size, relationshipType, hasApartment, sortBy, direction, keyword), "Lấy danh sách cư dân thành công");
    }

    @Operation(summary = "Lấy cư dân theo ID")
    @GetMapping("/{residentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<ResidentGetResponse>> getResidentById(@PathVariable Long residentId) {
        return success(residentService.getResidentById(residentId), "Lấy cư dân thành công");
    }

    @Operation(summary = "Lấy cư dân theo username")
    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<ResidentGetResponse>> getResidentByUsername(@PathVariable String username) {
        return success(residentService.getResidentByUsername(username), "Lấy cư dân thành công");
    }

    @Operation(summary = "Tạo mới cư dân")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> createResident(@RequestBody ResidentCreateRequest apiRequest) {
        return success(residentService.createResident(apiRequest), "Tạo mới cư dân thành công");
    }

    @Operation(summary = "Cập nhật cư dân (PATCH)")
    @PatchMapping("/{residentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<Long>> updateResident(@PathVariable Long residentId, @RequestBody ResidentUpdateRequest apiRequest) {
        return success(residentService.updateResident(residentId, apiRequest), "Cập nhật cư dân thành công");
    }

    @Operation(summary = "Xóa cư dân")
    @DeleteMapping("/{residentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteResident(@PathVariable Long residentId) {
        return success(residentService.deleteResident(residentId), "Xóa cư dân thành công");
    }
}
