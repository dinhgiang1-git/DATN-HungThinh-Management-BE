package dinhgiang.dev.hungthinh.services.implement;

import dinhgiang.dev.hungthinh.exceptions.UserMessageException;
import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentShortGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.contracts.ContractCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.contracts.ContractGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.contracts.ContractUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.bases.Apartment;
import dinhgiang.dev.hungthinh.models.entities.bases.Contract;
import dinhgiang.dev.hungthinh.models.entities.bases.Resident;
import dinhgiang.dev.hungthinh.models.entities.enums.ApartmentStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.ContractStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.ContractType;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import dinhgiang.dev.hungthinh.repositories.ApartmentRepository;
import dinhgiang.dev.hungthinh.repositories.ContractRepository;
import dinhgiang.dev.hungthinh.repositories.ResidentRepository;
import dinhgiang.dev.hungthinh.services.interfaces.IContractService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import dinhgiang.dev.hungthinh.components.Auditable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * Lớp cài đặt (Implementation) cho IContractService.
 * Xử lý các nghiệp vụ quản lý hợp đồng: CRUD và quản lý file đính kèm trên hệ thống.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Service
@RequiredArgsConstructor
public class ContractService implements IContractService {
    private final ContractRepository contractRepository;
    private final ApartmentRepository apartmentRepository;
    private final ResidentRepository residentRepository;
    private final FileStorageService fileStorageService;
    private final AccessControlService accessControlService;

    @Override
    public PageResponse<ContractGetResponse> getAllContracts(
            int page, int size,
            ContractStatus contractStatus,
            ContractType contractType,
            String sortBy, String direction,
            String keyword
    ) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Specification<Contract> spec = Specification.allOf();
        if (contractStatus != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("contractStatus"), contractStatus));
        }
        if (contractType != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("contractType"), contractType));
        }
        if (keyword != null && !keyword.isBlank()) {
            String likeKeyword = "%" + keyword.toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("contractNumber")), likeKeyword),
                            cb.like(cb.lower(root.get("note")), likeKeyword),
                            cb.like(cb.lower(root.join("apartment").get("apartmentNumber")), likeKeyword),
                            cb.like(cb.lower(root.join("resident").get("fullName")), likeKeyword)
                    )
            );
        }

        Page<Contract> contracts = contractRepository.findAll(spec, pageRequest);

        List<ContractGetResponse> content = contracts.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return new PageResponse<>(
                content,
                contracts.getNumber() + 1,
                contracts.getSize(),
                contracts.getTotalElements(),
                contracts.getTotalPages()
        );
    }

    @Override
    public ContractGetResponse getContractById(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new UserMessageException("Hợp đồng không tồn tại"));
        assertResidentCanAccessContract(contract);
        return mapToResponse(contract);
    }

    @Override
    @Auditable(action = "CREATE", entityType = "CONTRACT")
    @Transactional
    public Long createContract(ContractCreateRequest request, MultipartFile file) {
        String contractNumber = request.getContractNumber();
        validateContractDates(request.getStartDate(), request.getEndDate());

        // Tự sinh mã hợp đồng nếu không nhập
        if (contractNumber == null || contractNumber.isBlank()) {
            java.time.LocalDate now = java.time.LocalDate.now();
            String prefix = String.format("HD-%02d/%d-", now.getMonthValue(), now.getYear());
            long count = contractRepository.countByContractNumberStartingWith(prefix);
            // Tìm số thứ tự chưa trùng
            do {
                count++;
                contractNumber = prefix + String.format("%03d", count);
            } while (contractRepository.existsByContractNumber(contractNumber));
        } else {
            // Kiểm tra trùng mã hợp đồng khi nhập thủ công
            if (contractRepository.existsByContractNumber(contractNumber)) {
                throw new UserMessageException("Mã hợp đồng '" + contractNumber + "' đã tồn tại trong hệ thống");
            }
        }

        Contract contract = new Contract();
        contract.setContractNumber(contractNumber);
        contract.setContractType(ContractType.valueOf(request.getContractType()));
        contract.setContractStatus(ContractStatus.ACTIVE);
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        contract.setNote(request.getNote());

        Apartment apartment = null;
        if (request.getApartmentId() != null) {
            apartment = apartmentRepository.findById(request.getApartmentId())
                    .orElseThrow(() -> new UserMessageException("Căn hộ không tồn tại"));
            contract.setApartment(apartment);
        }

        Resident resident = null;
        if (request.getResidentId() != null) {
            resident = residentRepository.findById(request.getResidentId())
                    .orElseThrow(() -> new UserMessageException("Cư dân không tồn tại"));
            contract.setResident(resident);
        }
        validateResidentBelongsToApartment(apartment, resident);
        assertNoOverlappingActiveContract(apartment, contract.getStartDate(), contract.getEndDate(), null);

        // Lưu file
        if (file != null && !file.isEmpty()) {
            String storedFileName = fileStorageService.storeFile(file);
            contract.setFilePath(storedFileName);
            contract.setOriginalFileName(file.getOriginalFilename());
        }

        contractRepository.save(contract);
        syncApartmentByContracts(apartment);
        return contract.getId();
    }

    @Override
    @Auditable(action = "UPDATE", entityType = "CONTRACT")
    @Transactional
    public Long updateContract(Long contractId, ContractUpdateRequest request, MultipartFile file) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new UserMessageException("Hợp đồng không tồn tại"));
        Long oldApartmentId = contract.getApartment() != null ? contract.getApartment().getId() : null;

        if (request.getContractNumber() != null) {
            // Kiểm tra trùng mã hợp đồng (trừ chính nó)
            if (!request.getContractNumber().equals(contract.getContractNumber())
                    && contractRepository.existsByContractNumber(request.getContractNumber())) {
                throw new UserMessageException("Mã hợp đồng '" + request.getContractNumber() + "' đã tồn tại trong hệ thống");
            }
            contract.setContractNumber(request.getContractNumber());
        }
        if (request.getContractType() != null) {
            contract.setContractType(ContractType.valueOf(request.getContractType()));
        }
        if (request.getContractStatus() != null) {
            contract.setContractStatus(ContractStatus.valueOf(request.getContractStatus()));
        }
        if (request.getStartDate() != null) {
            contract.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            contract.setEndDate(request.getEndDate());
        }
        validateContractDates(contract.getStartDate(), contract.getEndDate());
        if (request.getNote() != null) {
            contract.setNote(request.getNote());
        }
        if (request.getApartmentId() != null) {
            Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                    .orElseThrow(() -> new UserMessageException("Căn hộ không tồn tại"));
            contract.setApartment(apartment);
        }
        if (request.getResidentId() != null) {
            Resident resident = residentRepository.findById(request.getResidentId())
                    .orElseThrow(() -> new UserMessageException("Cư dân không tồn tại"));
            contract.setResident(resident);
        }
        validateResidentBelongsToApartment(contract.getApartment(), contract.getResident());
        if (contract.getContractStatus() == ContractStatus.ACTIVE) {
            assertNoOverlappingActiveContract(contract.getApartment(), contract.getStartDate(), contract.getEndDate(), contract.getId());
        }

        // Cập nhật file mới (xóa file cũ)
        if (file != null && !file.isEmpty()) {
            // Xóa file cũ
            if (contract.getFilePath() != null) {
                fileStorageService.deleteFile(contract.getFilePath());
            }
            String storedFileName = fileStorageService.storeFile(file);
            contract.setFilePath(storedFileName);
            contract.setOriginalFileName(file.getOriginalFilename());
        }

        contractRepository.save(contract);
        if (oldApartmentId != null
                && (contract.getApartment() == null || !oldApartmentId.equals(contract.getApartment().getId()))) {
            apartmentRepository.findById(oldApartmentId).ifPresent(this::syncApartmentByContracts);
        }
        syncApartmentByContracts(contract.getApartment());
        return contract.getId();
    }

    @Override
    @Auditable(action = "DELETE", entityType = "CONTRACT")
    @Transactional
    public Void deleteContract(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new UserMessageException("Hợp đồng không tồn tại"));
        Apartment apartment = contract.getApartment();
        // Xóa file trên ổ đĩa
        if (contract.getFilePath() != null) {
            fileStorageService.deleteFile(contract.getFilePath());
        }
        contractRepository.delete(contract);
        contractRepository.flush();
        syncApartmentByContracts(apartment);
        return null;
    }

    @Override
    public Resource downloadContractFile(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new UserMessageException("Hợp đồng không tồn tại"));
        assertResidentCanAccessContract(contract);
        if (contract.getFilePath() == null || contract.getFilePath().isBlank()) {
            throw new UserMessageException("Hợp đồng này chưa có file đính kèm");
        }
        return fileStorageService.loadFileAsResource(contract.getFilePath());
    }

    @Override
    public String getOriginalFileName(Long contractId) {
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new UserMessageException("Hợp đồng không tồn tại"));
        assertResidentCanAccessContract(contract);
        return contract.getOriginalFileName();
    }

    private ContractGetResponse mapToResponse(Contract contract) {
        ContractGetResponse.ContractGetResponseBuilder builder = ContractGetResponse.builder()
                .contractId(contract.getId())
                .contractNumber(contract.getContractNumber())
                .contractType(contract.getContractType())
                .contractStatus(contract.getContractStatus())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .originalFileName(contract.getOriginalFileName())
                .note(contract.getNote())
                .createdAt(contract.getCreatedAt());

        if (contract.getApartment() != null) {
            builder.apartment(ApartmentShortGetResponse.builder()
                    .apartmentNumber(contract.getApartment().getApartmentNumber())
                    .floor(contract.getApartment().getFloor())
                    .block(contract.getApartment().getBlock())
                    .build());
        }

        if (contract.getResident() != null) {
            builder.resident(ContractGetResponse.ContractResidentResponse.builder()
                    .residentId(contract.getResident().getId())
                    .fullName(contract.getResident().getFullName())
                    .phoneNumber(contract.getResident().getPhoneNumber())
                    .email(contract.getResident().getEmail())
                    .relationship(contract.getResident().getRelationship() != null
                            ? contract.getResident().getRelationship().getDisplayName() : null)
                    .build());
        }

        return builder.build();
    }

    @Override
    public PageResponse<ContractGetResponse> getContractsByApartmentId(Long apartmentId, int page, int size, String sortBy, String direction) {
        accessControlService.assertResidentCanAccessApartment(
                apartmentId,
                "Bạn không có quyền xem hợp đồng của căn hộ này"
        );

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Specification<Contract> spec = (root, query, cb) ->
                cb.equal(root.get("apartment").get("id"), apartmentId);

        Page<Contract> contracts = contractRepository.findAll(spec, pageRequest);

        List<ContractGetResponse> content = contracts.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return new PageResponse<>(
                content,
                contracts.getNumber() + 1,
                contracts.getSize(),
                contracts.getTotalElements(),
                contracts.getTotalPages()
        );
    }

    private void assertResidentCanAccessContract(Contract contract) {
        if (!accessControlService.isResident()) {
            return;
        }
        if (contract.getApartment() != null) {
            accessControlService.assertResidentCanAccessApartment(
                    contract.getApartment(),
                    "Bạn không có quyền truy cập hợp đồng này"
            );
            return;
        }
        Resident resident = contract.getResident();
        if (resident == null || resident.getApartment() == null) {
            throw new UserMessageException("Bạn không có quyền truy cập hợp đồng này");
        }
        accessControlService.assertResidentCanAccessApartment(
                resident.getApartment(),
                "Bạn không có quyền truy cập hợp đồng này"
        );
    }

    private void validateContractDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new UserMessageException("Ngày bắt đầu hợp đồng không được để trống");
        }
        if (endDate != null && startDate.isAfter(endDate)) {
            throw new UserMessageException("Ngày bắt đầu hợp đồng không được sau ngày kết thúc");
        }
    }

    private void validateResidentBelongsToApartment(Apartment apartment, Resident resident) {
        if (apartment == null) {
            throw new UserMessageException("Hợp đồng phải gắn với căn hộ");
        }
        if (resident == null) {
            throw new UserMessageException("Hợp đồng phải gắn với cư dân");
        }
        if (resident.getApartment() == null || !apartment.getId().equals(resident.getApartment().getId())) {
            throw new UserMessageException("Cư dân không thuộc căn hộ của hợp đồng");
        }
    }

    private void assertNoOverlappingActiveContract(Apartment apartment, LocalDate startDate, LocalDate endDate, Long contractId) {
        if (apartment == null) {
            return;
        }
        long overlappingContracts = contractRepository.countOverlappingActiveContracts(
                apartment.getId(),
                ContractStatus.ACTIVE,
                startDate,
                endDate,
                contractId
        );
        if (overlappingContracts > 0) {
            throw new UserMessageException("Căn hộ đã có hợp đồng ACTIVE chồng lấn thời gian");
        }
    }

    private void syncApartmentByContracts(Apartment apartment) {
        if (apartment == null || apartment.getId() == null) {
            return;
        }

        List<Contract> activeContracts = contractRepository.findActiveContractsForApartmentAtDate(
                apartment.getId(),
                ContractStatus.ACTIVE,
                LocalDate.now()
        );
        if (!activeContracts.isEmpty()) {
            Contract currentContract = activeContracts.get(0);
            apartment.setOwnerId(currentContract.getResident() != null ? currentContract.getResident().getId() : null);
            apartment.setApartmentStatus(ApartmentStatus.OCCUPIED);
        } else {
            apartment.setOwnerId(null);
            if (apartment.getApartmentStatus() != ApartmentStatus.UNDER_MAINTENANCE) {
                apartment.setApartmentStatus(ApartmentStatus.VACANT);
            }
        }
        apartmentRepository.save(apartment);
    }
}
