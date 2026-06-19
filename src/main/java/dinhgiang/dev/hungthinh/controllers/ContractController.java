package dinhgiang.dev.hungthinh.controllers;

import dinhgiang.dev.hungthinh.models.dtos.contracts.ContractCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.contracts.ContractGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.contracts.ContractUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.enums.ContractStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.ContractType;
import dinhgiang.dev.hungthinh.models.entities.global.ApiResponse;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import dinhgiang.dev.hungthinh.services.interfaces.IContractService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/contracts")
@RequiredArgsConstructor
public class ContractController extends BaseController {
    private final IContractService contractService;

    @Operation(summary = "Lấy danh sách hợp đồng")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<ContractGetResponse>>> getAllContracts(
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng item trong một trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trạng thái hợp đồng: ACTIVE, EXPIRED, TERMINATED") @RequestParam(required = false) ContractStatus contractStatus,
            @Parameter(description = "Loại hợp đồng: RENT, PURCHASE, SERVICE") @RequestParam(required = false) ContractType contractType,
            @Parameter(description = "Sắp xếp theo") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Thứ tự sắp xếp") @RequestParam(defaultValue = "desc") String direction,
            @Parameter(description = "Tìm kiếm theo từ khóa") @RequestParam(name = "keyword", required = false) String keyword
    ) {
        return success(contractService.getAllContracts(page, size, contractStatus, contractType, sortBy, direction, keyword), "Lấy danh sách hợp đồng thành công");
    }

    @Operation(summary = "Lấy hợp đồng theo id")
    @GetMapping("/{contractId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ContractGetResponse>> getContractById(@PathVariable Long contractId) {
        return success(contractService.getContractById(contractId), "Lấy hợp đồng thành công");
    }

    @Operation(summary = "Tạo mới hợp đồng")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> createContract(
            @RequestPart("data") ContractCreateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        return success(contractService.createContract(request, file), "Tạo mới hợp đồng thành công");
    }

    @Operation(summary = "Cập nhật hợp đồng")
    @PatchMapping(value = "/{contractId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> updateContract(
            @PathVariable Long contractId,
            @RequestPart("data") ContractUpdateRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        return success(contractService.updateContract(contractId, request, file), "Cập nhật hợp đồng thành công");
    }

    @Operation(summary = "Xóa hợp đồng")
    @DeleteMapping("/{contractId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteContract(@PathVariable Long contractId) {
        return success(contractService.deleteContract(contractId), "Xóa hợp đồng thành công");
    }

    @Operation(summary = "Tải file hợp đồng")
    @GetMapping("/{contractId}/download")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Resource> downloadContractFile(@PathVariable Long contractId) {
        Resource resource = contractService.downloadContractFile(contractId);
        String originalFileName = contractService.getOriginalFileName(contractId);

        String encodedFileName = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFileName)
                .body(resource);
    }

    @Operation(summary = "Lấy danh sách hợp đồng theo căn hộ (dành cho cư dân)")
    @GetMapping("/apartment/{apartmentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<PageResponse<ContractGetResponse>>> getContractsByApartmentId(
            @PathVariable Long apartmentId,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng item trong một trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sắp xếp theo") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Thứ tự sắp xếp") @RequestParam(defaultValue = "desc") String direction
    ) {
        return success(contractService.getContractsByApartmentId(apartmentId, page, size, sortBy, direction), "Lấy danh sách hợp đồng theo căn hộ thành công");
    }

    @Operation(summary = "Tải file hợp đồng (cư dân)")
    @GetMapping("/{contractId}/download-resident")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<Resource> downloadContractFileResident(@PathVariable Long contractId) {
        Resource resource = contractService.downloadContractFile(contractId);
        String originalFileName = contractService.getOriginalFileName(contractId);

        String encodedFileName = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFileName)
                .body(resource);
    }
}
