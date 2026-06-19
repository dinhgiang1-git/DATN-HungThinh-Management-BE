package dinhgiang.dev.hungthinh.controllers;

import dinhgiang.dev.hungthinh.models.dtos.tableElectricTiers.TableElectricTierCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.tableElectricTiers.TableElectricTierGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.tableElectricTiers.TableElectricTierUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.global.ApiResponse;
import dinhgiang.dev.hungthinh.services.implement.TableElectricTierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/v1/table_electric_tiers")
@RequiredArgsConstructor
public class TableElectricTierController extends BaseController{
    private final TableElectricTierService tableElectricTierService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<List<TableElectricTierGetResponse>>> getAllTableElectricTiers() {
        return success(tableElectricTierService.getAllTableElectricTiers(), "Lấy danh sách bảng phí điện lũy tiến thành công");
    }

    @GetMapping("/calculator")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<BigDecimal>> calculatorElectricFeeWithTier(
            @RequestParam(name = "totalKwh", required = true) Long totalKwh,
            @RequestParam(name = "startDate", required = true) LocalDate startDate,
            @RequestParam(name = "endDate", required = true) LocalDate endDate,
            @RequestParam(name = "numberOfHouseholds", defaultValue = "1") Integer numberOfHouseholds
    ) {
        return success(tableElectricTierService.calculatorElectricFeeWithTier(totalKwh, startDate, endDate, numberOfHouseholds), "Tính toán giá điện lũy tiến thành công");
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> createTableElectricTier(@RequestBody TableElectricTierCreateRequest apiRequest) {
        return success(tableElectricTierService.createTableElectricTier(apiRequest), "Tạo mới bảng phí điện lũy tiến thành công");
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> updateTableElectricTierById(@PathVariable Long id, @RequestBody TableElectricTierUpdateRequest apiRequest) {
        return success(tableElectricTierService.updateTableElectricTier(id, apiRequest), "Cập nhật bảng phí điện lũy tiến thành công");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTableElectricTierById(@PathVariable Long id) {
        return success(tableElectricTierService.deleteTableElectricTier(id), "Xóa thành công bảng phí điện lũy tiến");
    }
}
