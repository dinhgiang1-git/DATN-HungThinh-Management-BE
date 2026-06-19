package dinhgiang.dev.hungthinh.services.interfaces;

import dinhgiang.dev.hungthinh.models.dtos.contracts.ContractCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.contracts.ContractGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.contracts.ContractUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.enums.ContractStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.ContractType;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Interface định nghĩa các nghiệp vụ liên quan đến quản lý Hợp đồng.
 * Bao gồm các thao tác CRUD, upload/download file hợp đồng.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface IContractService {

    /**
     * Lấy danh sách hợp đồng có phân trang và lọc.
     *
     * @param page           trang hiện tại (bắt đầu từ 1)
     * @param size           số lượng phần tử trên mỗi trang
     * @param contractStatus trạng thái hợp đồng cần lọc
     * @param contractType   loại hợp đồng cần lọc
     * @param sortBy         trường cần sắp xếp
     * @param direction      hướng sắp xếp (ASC/DESC)
     * @param keyword        từ khóa tìm kiếm (số hợp đồng)
     * @return PageResponse chứa danh sách ContractGetResponse
     */
    PageResponse<ContractGetResponse> getAllContracts(int page, int size, ContractStatus contractStatus, ContractType contractType, String sortBy, String direction, String keyword);

    /**
     * Lấy thông tin chi tiết một hợp đồng.
     *
     * @param contractId ID của hợp đồng
     * @return ContractGetResponse chứa thông tin hợp đồng
     */
    ContractGetResponse getContractById(Long contractId);

    /**
     * Tạo mới một hợp đồng kèm theo file đính kèm.
     *
     * @param request đối tượng chứa dữ liệu tạo hợp đồng
     * @param file    file hợp đồng đính kèm (pdf, doc, v.v.)
     * @return ID của hợp đồng vừa tạo
     */
    Long createContract(ContractCreateRequest request, MultipartFile file);

    /**
     * Cập nhật thông tin hợp đồng hiện có và thay thế file đính kèm nếu có.
     *
     * @param contractId ID của hợp đồng cần cập nhật
     * @param request    đối tượng chứa dữ liệu cập nhật
     * @param file       file hợp đồng mới (nếu có)
     * @return ID của hợp đồng vừa được cập nhật
     */
    Long updateContract(Long contractId, ContractUpdateRequest request, MultipartFile file);

    /**
     * Xóa hợp đồng và file đính kèm tương ứng.
     *
     * @param contractId ID của hợp đồng cần xóa
     * @return Void
     */
    Void deleteContract(Long contractId);

    /**
     * Tải xuống file hợp đồng đính kèm.
     *
     * @param contractId ID của hợp đồng
     * @return Resource đại diện cho file
     */
    Resource downloadContractFile(Long contractId);

    /**
     * Lấy tên gốc của file hợp đồng đính kèm.
     *
     * @param contractId ID của hợp đồng
     * @return Tên file gốc
     */
    String getOriginalFileName(Long contractId);

    /**
     * Lấy danh sách hợp đồng liên quan đến một căn hộ cụ thể.
     *
     * @param apartmentId ID của căn hộ
     * @param page        trang hiện tại
     * @param size        số phần tử trên mỗi trang
     * @param sortBy      trường cần sắp xếp
     * @param direction   hướng sắp xếp
     * @return PageResponse chứa danh sách ContractGetResponse
     */
    PageResponse<ContractGetResponse> getContractsByApartmentId(Long apartmentId, int page, int size, String sortBy, String direction);
}
