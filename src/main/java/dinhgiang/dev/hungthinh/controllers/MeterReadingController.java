package dinhgiang.dev.hungthinh.controllers;

import dinhgiang.dev.hungthinh.models.dtos.meterreadings.MeterReadingGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.meterreadings.MeterReadingPreviewResponse;
import dinhgiang.dev.hungthinh.models.entities.bases.UtilityMeterReading;
import dinhgiang.dev.hungthinh.models.entities.global.ApiResponse;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import dinhgiang.dev.hungthinh.services.implement.MeterReadingService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/meter-readings")
@RequiredArgsConstructor
public class MeterReadingController extends BaseController {
    private final MeterReadingService meterReadingService;

    @Operation(summary = "Lấy lịch sử ghi chỉ số điện nước")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<PageResponse<MeterReadingGetResponse>>> getAll(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "apartmentId", required = false) Long apartmentId,
            @RequestParam(name = "billingPeriod", required = false) String billingPeriod
    ) {
        return success(meterReadingService.getAll(page, size, apartmentId, billingPeriod), "Lấy lịch sử chỉ số thành công");
    }

    @Operation(summary = "Xem trước chỉ số kỳ tới từ mock API")
    @GetMapping("/preview")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<MeterReadingPreviewResponse>> preview(
            @RequestParam Long apartmentId,
            @RequestParam(required = false) String billingPeriod
    ) {
        return success(meterReadingService.previewNextReading(apartmentId, billingPeriod), "Lấy chỉ số dự kiến thành công");
    }

    @Operation(summary = "Technician ghi chỉ số điện nước thủ công kèm ảnh")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<MeterReadingGetResponse>> createManualReading(
            @RequestParam Long apartmentId,
            @RequestParam(required = false) String billingPeriod,
            @RequestParam(required = false) BigDecimal electricCurrentReading,
            @RequestParam(required = false) BigDecimal waterCurrentReading,
            @RequestParam(required = false) String note,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "electricFile", required = false) MultipartFile electricFile,
            @RequestPart(value = "waterFile", required = false) MultipartFile waterFile
    ) {
        return success(
                meterReadingService.recordManualReading(apartmentId, billingPeriod, electricCurrentReading, waterCurrentReading, note, file, electricFile, waterFile),
                "Ghi chỉ số điện nước thành công"
        );
    }

    @Operation(summary = "Xem ảnh kiểm chứng chỉ số điện nước")
    @GetMapping("/{meterReadingId}/evidence")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    public ResponseEntity<Resource> getEvidence(@PathVariable Long meterReadingId) {
        Resource resource = meterReadingService.loadEvidenceAsResource(meterReadingId);
        UtilityMeterReading reading = meterReadingService.getReading(meterReadingId);
        String originalFileName = reading.getEvidenceOriginalFileName() != null
                ? reading.getEvidenceOriginalFileName()
                : "meter-evidence";
        String encodedFileName = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8)
                .replace("+", "%20");
        String contentType = reading.getEvidenceContentType() != null
                ? reading.getEvidenceContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encodedFileName)
                .body(resource);
    }

    @Operation(summary = "Xem ảnh kiểm chứng theo loại chỉ số")
    @GetMapping("/{meterReadingId}/evidence/{type}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN')")
    public ResponseEntity<Resource> getEvidenceByType(
            @PathVariable Long meterReadingId,
            @PathVariable String type
    ) {
        Resource resource = meterReadingService.loadEvidenceAsResource(meterReadingId, type);
        UtilityMeterReading reading = meterReadingService.getReading(meterReadingId);
        boolean electric = "electric".equalsIgnoreCase(type);
        String originalFileName = electric
                ? firstNonBlank(reading.getElectricEvidenceOriginalFileName(), reading.getEvidenceOriginalFileName(), "meter-electric-evidence")
                : firstNonBlank(reading.getWaterEvidenceOriginalFileName(), reading.getEvidenceOriginalFileName(), "meter-water-evidence");
        String encodedFileName = URLEncoder.encode(originalFileName, StandardCharsets.UTF_8)
                .replace("+", "%20");
        String contentType = electric
                ? firstNonBlank(reading.getElectricEvidenceContentType(), reading.getEvidenceContentType(), MediaType.APPLICATION_OCTET_STREAM_VALUE)
                : firstNonBlank(reading.getWaterEvidenceContentType(), reading.getEvidenceContentType(), MediaType.APPLICATION_OCTET_STREAM_VALUE);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename*=UTF-8''" + encodedFileName)
                .body(resource);
    }

    private String firstNonBlank(String first, String second, String fallback) {
        if (first != null && !first.isBlank()) return first;
        if (second != null && !second.isBlank()) return second;
        return fallback;
    }
}
