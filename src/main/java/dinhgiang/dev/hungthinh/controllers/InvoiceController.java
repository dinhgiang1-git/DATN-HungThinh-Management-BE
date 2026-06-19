package dinhgiang.dev.hungthinh.controllers;

import dinhgiang.dev.hungthinh.models.dtos.invoices.InvoiceBatchCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.invoices.InvoiceCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.invoices.InvoiceGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.invoices.InvoiceUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.enums.InvoiceStatus;
import dinhgiang.dev.hungthinh.models.entities.global.ApiResponse;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import dinhgiang.dev.hungthinh.services.interfaces.IInvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController extends BaseController {

    private final IInvoiceService invoiceService;

    @Operation(summary = "Lấy danh sách hóa đơn")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT') or hasRole('TECHNICIAN')")
    public ResponseEntity<ApiResponse<PageResponse<InvoiceGetResponse>>> getAllInvoices(
            @Parameter(description = "Số trang") @RequestParam(name = "page", defaultValue = "0") int page,
            @Parameter(description = "Số lượng phần tử trong một trang") @RequestParam(name = "size", defaultValue = "10") int size,
            @Parameter(description = "Trạng thái phản hồi: PAID, UNPAID") @RequestParam(name = "invoiceStatus", required = false) InvoiceStatus invoiceStatus,
            @Parameter(description = "Sẵp xếp theo id") @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @Parameter(description = "Theo thứ tự asc") @RequestParam(name = "direction", defaultValue = "asc") String direction,
            @Parameter(description = "Từ khóa tìm kiếm (Theo InvoiceNumber, DueDate)") @RequestParam(name = "keyword", required = false) String keyword,
            @Parameter(description = "Lọc theo căn hộ") @RequestParam(name = "apartmentId", required = false) Long apartmentId,
            @Parameter(description = "Kỳ hóa đơn yyyy-MM") @RequestParam(name = "billingPeriod", required = false) String billingPeriod
    ) {
        return success(invoiceService.getAllInvoices(page, size, invoiceStatus, sortBy, direction, keyword, apartmentId, billingPeriod), "Lấy hóa đơn thành công");
    }

    @Operation(summary = "Lấy hóa đơn theo ID")
    @GetMapping("/{invoiceId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RESIDENT')")
    public ResponseEntity<ApiResponse<InvoiceGetResponse>> getInvoiceById(@PathVariable("invoiceId") Long invoiceId) {
        return success(invoiceService.getInvoiceById(invoiceId), "Lấy hóa đơn thành công");
    }

    @Operation(summary = "Tạo mới hóa đơn")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> createInvoice(@RequestBody InvoiceCreateRequest apiRequest) {
        return success(invoiceService.createInvoice(apiRequest), "Tạo mới hóa đơn thành công");
    }

    @Operation(summary = "Tạo hóa đơn hàng loạt")
    @PostMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Long>>> batchCreateInvoices(@RequestBody InvoiceBatchCreateRequest request) {
        return success(invoiceService.batchCreateInvoices(request), "Tạo hóa đơn hàng loạt thành công");
    }

    @Operation(summary = "Cập nhật hóa đơn")
    @PatchMapping("/{invoiceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> updateInvoice(@RequestBody InvoiceUpdateRequest apiRequest, @PathVariable("invoiceId") Long invoiceId) {
        return success(invoiceService.updateInvoice(invoiceId, apiRequest), "Cập nhật hóa đơn thành công");
    }

    @Operation(summary = "Xóa hóa đơn")
    @DeleteMapping("/{invoiceId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteInvoice(@PathVariable("invoiceId") Long invoiceId) {
        return success(invoiceService.deleteInvoice(invoiceId), "Xóa hóa đơn thành công");
    }
}
