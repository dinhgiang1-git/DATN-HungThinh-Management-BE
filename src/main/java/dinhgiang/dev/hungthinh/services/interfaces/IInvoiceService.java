package dinhgiang.dev.hungthinh.services.interfaces;

import dinhgiang.dev.hungthinh.models.dtos.invoices.InvoiceBatchCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.invoices.InvoiceCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.invoices.InvoiceGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.invoices.InvoiceUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.enums.InvoiceStatus;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;

import java.util.List;

/**
 * Interface định nghĩa các nghiệp vụ liên quan đến quản lý Hóa đơn phí dịch vụ.
 * Cung cấp chức năng tạo đơn lẻ, tạo hàng loạt và quản lý trạng thái thanh toán.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface IInvoiceService {

    /**
     * Lấy danh sách hóa đơn có phân trang và lọc theo trạng thái, từ khóa.
     *
     * @param page          trang hiện tại
     * @param size          số lượng phần tử trên mỗi trang
     * @param invoiceStatus trạng thái hóa đơn (PAID, UNPAID, ...)
     * @param sortBy        trường cần sắp xếp
     * @param direction     hướng sắp xếp
     * @param keyword       từ khóa tìm kiếm (số hóa đơn)
     * @param apartmentId   ID căn hộ
     * @return PageResponse chứa danh sách InvoiceGetResponse
     */
    PageResponse<InvoiceGetResponse> getAllInvoices(int page, int size, InvoiceStatus invoiceStatus, String sortBy, String direction, String keyword, Long apartmentId, String billingPeriod);

    /**
     * Lấy thông tin chi tiết một hóa đơn.
     *
     * @param invoiceId ID của hóa đơn
     * @return InvoiceGetResponse chứa thông tin chi tiết hóa đơn
     */
    InvoiceGetResponse getInvoiceById(Long invoiceId);

    /**
     * Tạo mới một hóa đơn cho một căn hộ.
     *
     * @param apiRequest đối tượng chứa dữ liệu tạo hóa đơn
     * @return ID của hóa đơn vừa tạo
     */
    Long createInvoice(InvoiceCreateRequest apiRequest);

    /**
     * Cập nhật thông tin hóa đơn.
     *
     * @param invoiceId  ID của hóa đơn cần cập nhật
     * @param apiRequest đối tượng chứa dữ liệu cập nhật
     * @return ID của hóa đơn vừa được cập nhật
     */
    Long updateInvoice(Long invoiceId, InvoiceUpdateRequest apiRequest);

    /**
     * Xóa một hóa đơn (chỉ nên cho phép khi chưa thanh toán).
     *
     * @param invoiceId ID của hóa đơn cần xóa
     * @return Void
     */
    Void deleteInvoice(Long invoiceId);

    /**
     * Tạo hóa đơn hàng loạt cho nhiều căn hộ trong cùng một kỳ.
     * Tính toán tự động các loại phí dựa trên bảng phí (TableFee) và chỉ số điện/nước.
     *
     * @param request đối tượng chứa dữ liệu kỳ thanh toán và hạn chót
     * @return Danh sách các ID hóa đơn vừa được tạo
     */
    List<Long> batchCreateInvoices(InvoiceBatchCreateRequest request);
}
