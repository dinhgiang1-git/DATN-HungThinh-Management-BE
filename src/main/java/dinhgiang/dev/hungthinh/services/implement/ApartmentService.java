package dinhgiang.dev.hungthinh.services.implement;

import dinhgiang.dev.hungthinh.components.Auditable;
import dinhgiang.dev.hungthinh.exceptions.UserMessageException;
import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentReportItemResponse;
import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentStatisticsResponse;
import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentUpdateRequest;
import dinhgiang.dev.hungthinh.models.dtos.contracts.ContractShortGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.residents.ResidentShortGetResponse;
import dinhgiang.dev.hungthinh.models.entities.bases.Apartment;
import dinhgiang.dev.hungthinh.models.entities.bases.Contract;
import dinhgiang.dev.hungthinh.models.entities.bases.Invoice;
import dinhgiang.dev.hungthinh.models.entities.bases.Resident;
import dinhgiang.dev.hungthinh.models.entities.enums.ApartmentStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.ContractStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.InvoiceStatus;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import dinhgiang.dev.hungthinh.repositories.ApartmentRepository;
import dinhgiang.dev.hungthinh.repositories.DeviceRepository;
import dinhgiang.dev.hungthinh.repositories.ResidentRepository;
import dinhgiang.dev.hungthinh.services.interfaces.IApartmentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lớp cài đặt (Implementation) cho IApartmentService.
 * Xử lý các nghiệp vụ quản lý căn hộ: tìm kiếm, thêm, sửa, xóa và gán cư dân.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Service
@RequiredArgsConstructor
public class ApartmentService implements IApartmentService {
    private static final String DEFAULT_COMPLEX_NAME = "Hưng Thịnh";

    public final ApartmentRepository apartmentRepository;
    private final ResidentRepository residentRepository;
    private final DeviceRepository deviceRepository;

    public PageResponse<ApartmentGetResponse> getAllApartment(
            int page,
            int size,
            ApartmentStatus apartmentStatus,
            String sortBy,
            String direction,
            String keyword,
            String complexName,
            String block,
            Integer floor,
            Boolean hasDevices
    ) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Specification<Apartment> spec = buildApartmentSpecification(apartmentStatus, keyword, complexName, block, floor, hasDevices);
        Page<Apartment> apartments = apartmentRepository.findAll(spec, pageRequest);

        List<ApartmentGetResponse> responses = apartments.getContent().stream()
                .map(this::buildApartmentResponse)
                .toList();

        return new PageResponse<>(
                responses,
                apartments.getNumber() + 1,
                apartments.getSize(),
                apartments.getTotalElements(),
                apartments.getTotalPages()
        );
    }

    public ApartmentStatisticsResponse getApartmentStatistics(String groupBy, String complexName, String block, Integer floor) {
        String normalizedGroupBy = normalizeGroupBy(groupBy);
        Specification<Apartment> spec = buildApartmentSpecification(null, null, complexName, block, floor, null);
        List<Apartment> apartments = apartmentRepository.findAll(spec);

        ReportAccumulator total = new ReportAccumulator("TOTAL", "Tổng hợp", null, null, null, null, null);
        Map<String, ReportAccumulator> groups = new LinkedHashMap<>();

        for (Apartment apartment : apartments) {
            total.addApartment(apartment);
            String key = resolveGroupKey(normalizedGroupBy, apartment);
            groups.computeIfAbsent(key, ignored -> newGroupAccumulator(normalizedGroupBy, apartment))
                    .addApartment(apartment);
        }

        List<ApartmentReportItemResponse> items = groups.values().stream()
                .sorted(Comparator.comparing(ReportAccumulator::sortLabel, String.CASE_INSENSITIVE_ORDER))
                .map(ReportAccumulator::toResponse)
                .toList();

        return ApartmentStatisticsResponse.builder()
                .groupBy(normalizedGroupBy)
                .complexName(complexName)
                .block(block)
                .floor(floor)
                .apartmentCount(total.apartmentCount)
                .occupiedCount(total.occupiedCount)
                .vacantCount(total.vacantCount)
                .maintenanceCount(total.maintenanceCount)
                .residentCount(total.residentCount)
                .deviceCount(total.deviceCount)
                .invoiceCount(total.invoiceCount)
                .unpaidInvoiceCount(total.unpaidInvoiceCount)
                .totalRevenue(total.totalRevenue)
                .paidRevenue(total.paidRevenue)
                .unpaidRevenue(total.unpaidRevenue)
                .items(items)
                .build();
    }

    public ApartmentGetResponse getApartmentById(Long apartmentId) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new UserMessageException("Căn hộ không tồn tại"));
        return buildApartmentResponse(apartment);
    }

    public ApartmentGetResponse getApartmentByResident(Long residentId) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new UserMessageException("Cư dân không tồn tại"));
        Apartment apartment = apartmentRepository.findById(resident.getApartment().getId())
                .orElseThrow(() -> new UserMessageException("Căn hộ không tồn tại"));
        return buildApartmentResponse(apartment);
    }

    @Transactional
    @Auditable(action = "CREATE", entityType = "APARTMENT")
    public Long createApartment(ApartmentCreateRequest apiRequest) {
        String complexName = normalizeComplexName(apiRequest.getComplexName());
        assertApartmentNumberAvailable(null, complexName, apiRequest.getBlock(), apiRequest.getApartmentNumber());

        Apartment apartment = new Apartment();
        apartment.setComplexName(complexName);
        apartment.setApartmentNumber(apiRequest.getApartmentNumber());
        apartment.setFloor(apiRequest.getFloor());
        apartment.setBlock(apiRequest.getBlock());
        apartment.setArea(apiRequest.getArea());
        apartment.setOwnerId(apiRequest.getOwnerId());
        apartment.setApartmentStatus(resolveApartmentStatus(apiRequest.getOwnerId(), apiRequest.getApartmentStatus()));
        apartmentRepository.save(apartment);
        assignResidentsToApartment(apartment, apiRequest.getOwnerId(), apiRequest.getResidentIds());

        return apartment.getId();
    }

    @Transactional
    @Auditable(action = "UPDATE", entityType = "APARTMENT")
    public Long updateApartment(Long apartmentId, ApartmentUpdateRequest apiRequest) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new UserMessageException("Căn hộ không tồn tại"));

        String nextComplexName = apiRequest.getComplexName() != null
                ? normalizeComplexName(apiRequest.getComplexName())
                : normalizeComplexName(apartment.getComplexName());
        String nextBlock = apiRequest.getBlock() != null ? apiRequest.getBlock() : apartment.getBlock();
        String nextApartmentNumber = apiRequest.getApartmentNumber() != null ? apiRequest.getApartmentNumber() : apartment.getApartmentNumber();
        assertApartmentNumberAvailable(apartmentId, nextComplexName, nextBlock, nextApartmentNumber);

        if (apiRequest.getComplexName() != null) {
            apartment.setComplexName(nextComplexName);
        }
        if (apiRequest.getApartmentNumber() != null) {
            apartment.setApartmentNumber(apiRequest.getApartmentNumber());
        }
        if (apiRequest.getFloor() != null) {
            apartment.setFloor(apiRequest.getFloor());
        }
        if (apiRequest.getBlock() != null) {
            apartment.setBlock(apiRequest.getBlock());
        }
        if (apiRequest.getArea() != null) {
            apartment.setArea(apiRequest.getArea());
        }
        if (apiRequest.getOwnerId() != null) {
            Resident resident = residentRepository.findById(apiRequest.getOwnerId())
                    .orElseThrow(() -> new UserMessageException("Chủ căn hộ không tồn tại"));
            apartment.setOwnerId(resident.getId());
            resident.setApartment(apartment);
        }
        if (apiRequest.getResidentIds() != null) {
            List<Resident> residents = residentRepository.findAllById(apiRequest.getResidentIds());
            if (residents.size() != apiRequest.getResidentIds().size()) {
                throw new UserMessageException("Một số cư dân không tồn tại");
            }
            for (Resident resident : residents) {
                resident.setApartment(apartment);
            }
        }
        if (apiRequest.getOwnerId() != null) {
            apartment.setApartmentStatus(ApartmentStatus.OCCUPIED);
        } else if (apiRequest.getApartmentStatus() != null) {
            apartment.setApartmentStatus(apiRequest.getApartmentStatus());
        }

        apartmentRepository.save(apartment);
        return apartment.getId();
    }

    @Auditable(action = "DELETE", entityType = "APARTMENT")
    public Void deleteApartment(Long apartmentId) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new UserMessageException("Căn hộ không tồn tại"));
        if (apartment.getResidents() != null && !apartment.getResidents().isEmpty()) {
            throw new UserMessageException("Không thể xóa căn hộ đang có cư dân");
        }
        if (apartment.getInvoices() != null && apartment.getInvoices().stream()
                .anyMatch(inv -> inv.getInvoiceStatus() != InvoiceStatus.PAID)) {
            throw new UserMessageException("Không thể xóa căn hộ còn hóa đơn chưa thanh toán");
        }
        if (apartment.getContracts() != null && apartment.getContracts().stream()
                .anyMatch(c -> c.getContractStatus() == ContractStatus.ACTIVE)) {
            throw new UserMessageException("Không thể xóa căn hộ còn hợp đồng đang hiệu lực");
        }
        apartmentRepository.delete(apartment);
        return null;
    }

    private Specification<Apartment> buildApartmentSpecification(
            ApartmentStatus apartmentStatus,
            String keyword,
            String complexName,
            String block,
            Integer floor,
            Boolean hasDevices
    ) {
        Specification<Apartment> spec = Specification.allOf();
        if (apartmentStatus != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("apartmentStatus"), apartmentStatus));
        }
        if (complexName != null && !complexName.isBlank()) {
            String normalizedComplexName = normalizeComplexName(complexName);
            spec = spec.and((root, query, cb) -> {
                if (DEFAULT_COMPLEX_NAME.equalsIgnoreCase(normalizedComplexName)) {
                    return cb.or(
                            cb.equal(cb.lower(root.get("complexName")), normalizedComplexName.toLowerCase()),
                            cb.isNull(root.get("complexName")),
                            cb.equal(root.get("complexName"), "")
                    );
                }
                return cb.equal(cb.lower(root.get("complexName")), normalizedComplexName.toLowerCase());
            });
        }
        if (block != null && !block.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("block")), block.toLowerCase()));
        }
        if (floor != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("floor"), floor));
        }
        if (keyword != null && !keyword.isBlank()) {
            String likeKeyword = "%" + keyword.toLowerCase() + "%";
            Specification<Apartment> keywordSpec = (root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("apartmentNumber")), likeKeyword),
                            cb.like(cb.lower(root.get("block")), likeKeyword),
                            cb.like(cb.lower(root.get("complexName")), likeKeyword)
                    );
            spec = spec.and(keywordSpec);
        }
        if (hasDevices != null) {
            spec = spec.and(hasDevices
                    ? (root, query, cb) -> cb.isNotEmpty(root.get("devices"))
                    : (root, query, cb) -> cb.isEmpty(root.get("devices")));
        }
        return spec;
    }

    private ApartmentGetResponse buildApartmentResponse(Apartment apartment) {
        return ApartmentGetResponse.builder()
                .id(apartment.getId())
                .complexName(resolveComplexName(apartment.getComplexName()))
                .apartmentNumber(apartment.getApartmentNumber())
                .floor(apartment.getFloor())
                .block(apartment.getBlock())
                .area(apartment.getArea())
                .ownerId(apartment.getOwnerId())
                .residents(safeList(apartment.getResidents()).stream()
                        .map(this::buildResidentShortResponse)
                        .toList())
                .contracts(safeList(apartment.getContracts()).stream()
                        .map(this::buildContractShortResponse)
                        .toList())
                .apartmentStatus(apartment.getApartmentStatus())
                .deviceCount(deviceRepository.countByApartmentId(apartment.getId()))
                .build();
    }

    private ResidentShortGetResponse buildResidentShortResponse(Resident resident) {
        return ResidentShortGetResponse.builder()
                .residentId(resident.getId())
                .fullName(resident.getFullName())
                .email(resident.getEmail())
                .phone(resident.getPhoneNumber())
                .relationshipType(resident.getRelationship())
                .build();
    }

    private ContractShortGetResponse buildContractShortResponse(Contract contract) {
        return ContractShortGetResponse.builder()
                .contractId(contract.getId())
                .contractNumber(contract.getContractNumber())
                .contractType(contract.getContractType())
                .contractStatus(contract.getContractStatus())
                .startDate(contract.getStartDate())
                .endDate(contract.getEndDate())
                .residentName(contract.getResident() != null ? contract.getResident().getFullName() : null)
                .originalFileName(contract.getOriginalFileName())
                .build();
    }

    private void assignResidentsToApartment(Apartment apartment, Long ownerId, List<Long> residentIds) {
        Set<Long> allResidentIds = new HashSet<>();
        if (residentIds != null) {
            allResidentIds.addAll(residentIds);
        }
        if (ownerId != null) {
            allResidentIds.add(ownerId);
        }
        if (allResidentIds.isEmpty()) {
            return;
        }

        List<Resident> residents = residentRepository.findAllById(allResidentIds);
        if (residents.size() != allResidentIds.size()) {
            throw new UserMessageException("Một số cư dân không tồn tại");
        }
        for (Resident resident : residents) {
            resident.setApartment(apartment);
        }
        residentRepository.saveAll(residents);
    }

    private void assertApartmentNumberAvailable(Long currentApartmentId, String complexName, String block, String apartmentNumber) {
        Specification<Apartment> spec = buildApartmentSpecification(null, null, complexName, block, null, null)
                .and((root, query, cb) -> cb.equal(cb.lower(root.get("apartmentNumber")), apartmentNumber.toLowerCase()));

        boolean duplicated = apartmentRepository.findAll(spec).stream()
                .anyMatch(apartment -> currentApartmentId == null || !currentApartmentId.equals(apartment.getId()));

        if (duplicated) {
            throw new UserMessageException("Căn hộ số " + apartmentNumber + " đã tồn tại trong khu " + complexName + ", tòa " + block);
        }
    }

    private ApartmentStatus resolveApartmentStatus(Long ownerId, ApartmentStatus requestedStatus) {
        if (ownerId != null) {
            return ApartmentStatus.OCCUPIED;
        }
        return requestedStatus != null ? requestedStatus : ApartmentStatus.VACANT;
    }

    private String normalizeComplexName(String complexName) {
        return complexName == null || complexName.isBlank() ? DEFAULT_COMPLEX_NAME : complexName.trim();
    }

    private String resolveComplexName(String complexName) {
        return normalizeComplexName(complexName);
    }

    private String normalizeGroupBy(String groupBy) {
        if (groupBy == null || groupBy.isBlank()) {
            return "COMPLEX";
        }
        return switch (groupBy.trim().toUpperCase()) {
            case "BLOCK", "FLOOR", "APARTMENT" -> groupBy.trim().toUpperCase();
            default -> "COMPLEX";
        };
    }

    private String resolveGroupKey(String groupBy, Apartment apartment) {
        String complexName = resolveComplexName(apartment.getComplexName());
        return switch (groupBy) {
            case "BLOCK" -> complexName + "|" + apartment.getBlock();
            case "FLOOR" -> complexName + "|" + apartment.getBlock() + "|" + apartment.getFloor();
            case "APARTMENT" -> String.valueOf(apartment.getId());
            default -> complexName;
        };
    }

    private ReportAccumulator newGroupAccumulator(String groupBy, Apartment apartment) {
        String complexName = resolveComplexName(apartment.getComplexName());
        String block = apartment.getBlock();
        Integer floor = apartment.getFloor();
        return switch (groupBy) {
            case "BLOCK" -> new ReportAccumulator(
                    complexName + "|" + block,
                    complexName + " / Tòa " + block,
                    complexName,
                    block,
                    null,
                    null,
                    null
            );
            case "FLOOR" -> new ReportAccumulator(
                    complexName + "|" + block + "|" + floor,
                    complexName + " / Tòa " + block + " / Tầng " + floor,
                    complexName,
                    block,
                    floor,
                    null,
                    null
            );
            case "APARTMENT" -> new ReportAccumulator(
                    String.valueOf(apartment.getId()),
                    complexName + " / " + block + "-" + apartment.getApartmentNumber(),
                    complexName,
                    block,
                    floor,
                    apartment.getId(),
                    apartment.getApartmentNumber()
            );
            default -> new ReportAccumulator(complexName, complexName, complexName, null, null, null, null);
        };
    }

    private <T> List<T> safeList(List<T> values) {
        return values != null ? values : Collections.emptyList();
    }

    private static BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static class ReportAccumulator {
        private final String groupKey;
        private final String groupLabel;
        private final String complexName;
        private final String block;
        private final Integer floor;
        private final Long apartmentId;
        private final String apartmentNumber;

        private long apartmentCount;
        private long occupiedCount;
        private long vacantCount;
        private long maintenanceCount;
        private long residentCount;
        private long deviceCount;
        private long invoiceCount;
        private long unpaidInvoiceCount;
        private BigDecimal totalRevenue = BigDecimal.ZERO;
        private BigDecimal paidRevenue = BigDecimal.ZERO;
        private BigDecimal unpaidRevenue = BigDecimal.ZERO;

        private ReportAccumulator(String groupKey, String groupLabel, String complexName, String block, Integer floor, Long apartmentId, String apartmentNumber) {
            this.groupKey = groupKey;
            this.groupLabel = groupLabel;
            this.complexName = complexName;
            this.block = block;
            this.floor = floor;
            this.apartmentId = apartmentId;
            this.apartmentNumber = apartmentNumber;
        }

        private void addApartment(Apartment apartment) {
            apartmentCount++;
            if (apartment.getApartmentStatus() == ApartmentStatus.OCCUPIED) {
                occupiedCount++;
            } else if (apartment.getApartmentStatus() == ApartmentStatus.VACANT) {
                vacantCount++;
            } else if (apartment.getApartmentStatus() == ApartmentStatus.UNDER_MAINTENANCE) {
                maintenanceCount++;
            }

            residentCount += apartment.getResidents() != null ? apartment.getResidents().size() : 0;
            deviceCount += apartment.getDevices() != null ? apartment.getDevices().size() : 0;
            List<Invoice> invoices = apartment.getInvoices() != null ? apartment.getInvoices() : new ArrayList<>();
            invoiceCount += invoices.size();

            for (Invoice invoice : invoices) {
                BigDecimal amount = safe(invoice.getTotalAmount());
                totalRevenue = totalRevenue.add(amount);
                if (invoice.getInvoiceStatus() == InvoiceStatus.PAID) {
                    paidRevenue = paidRevenue.add(amount);
                } else {
                    unpaidInvoiceCount++;
                    unpaidRevenue = unpaidRevenue.add(amount);
                }
            }
        }

        private String sortLabel() {
            return groupLabel;
        }

        private ApartmentReportItemResponse toResponse() {
            return ApartmentReportItemResponse.builder()
                    .groupKey(groupKey)
                    .groupLabel(groupLabel)
                    .complexName(complexName)
                    .block(block)
                    .floor(floor)
                    .apartmentId(apartmentId)
                    .apartmentNumber(apartmentNumber)
                    .apartmentCount(apartmentCount)
                    .occupiedCount(occupiedCount)
                    .vacantCount(vacantCount)
                    .maintenanceCount(maintenanceCount)
                    .residentCount(residentCount)
                    .deviceCount(deviceCount)
                    .invoiceCount(invoiceCount)
                    .unpaidInvoiceCount(unpaidInvoiceCount)
                    .totalRevenue(totalRevenue)
                    .paidRevenue(paidRevenue)
                    .unpaidRevenue(unpaidRevenue)
                    .build();
        }
    }
}
