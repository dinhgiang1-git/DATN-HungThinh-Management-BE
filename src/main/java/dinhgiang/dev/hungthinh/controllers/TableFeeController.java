package dinhgiang.dev.hungthinh.controllers;

import dinhgiang.dev.hungthinh.models.dtos.tableFees.TableFeeCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.tableFees.TableFeeGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.tableFees.TableFeeUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.global.ApiResponse;
import dinhgiang.dev.hungthinh.services.implement.TableFeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tablefees")
@RequiredArgsConstructor
public class TableFeeController extends BaseController{
    private final TableFeeService tableFeeService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<List<TableFeeGetResponse>>> getAllTableFee() {
        return success(tableFeeService.getAllTableFee(), "Lấy bảng phí thành công");
    }

    @PatchMapping("/{tableFeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> updateTableFee(@PathVariable Long tableFeeId, TableFeeUpdateRequest apiRequest) {
        return success(tableFeeService.updateTableFee(tableFeeId, apiRequest), "Cập nhật bảng phí thành công");
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> createTableFee(TableFeeCreateRequest apiRequest) {
        return success(tableFeeService.createTableFee(apiRequest), "Tạo mới bảng phí thành công");
    }

    @DeleteMapping("/{tableFeeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTableFee(@PathVariable Long tableFeeId) {
        return success(tableFeeService.deleteTableFee(tableFeeId), "Xóa bảng phí thành công");
    }
}
