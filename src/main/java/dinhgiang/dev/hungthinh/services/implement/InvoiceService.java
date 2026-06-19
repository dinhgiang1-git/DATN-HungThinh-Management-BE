package dinhgiang.dev.hungthinh.services.implement;

import dinhgiang.dev.hungthinh.exceptions.UserMessageException;
import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentShortGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.invoices.InvoiceBatchCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.invoices.InvoiceCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.invoices.InvoiceGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.invoices.InvoiceUpdateRequest;
import dinhgiang.dev.hungthinh.models.dtos.meterreadings.MeterReadingPreviewResponse;
import dinhgiang.dev.hungthinh.models.dtos.payments.PaymentGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.users.UserShortGetResponse;
import dinhgiang.dev.hungthinh.models.entities.bases.Apartment;
import dinhgiang.dev.hungthinh.models.entities.bases.Invoice;
import dinhgiang.dev.hungthinh.models.entities.bases.Payment;
import dinhgiang.dev.hungthinh.models.entities.bases.TableFee;
import dinhgiang.dev.hungthinh.models.entities.bases.User;
import dinhgiang.dev.hungthinh.models.entities.enums.InvoiceStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.MeterReadingSource;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import dinhgiang.dev.hungthinh.repositories.ApartmentRepository;
import dinhgiang.dev.hungthinh.repositories.InvoiceRepository;
import dinhgiang.dev.hungthinh.repositories.ResidentRepository;
import dinhgiang.dev.hungthinh.repositories.TableFeeRepository;
import dinhgiang.dev.hungthinh.repositories.UtilityMeterReadingRepository;
import dinhgiang.dev.hungthinh.repositories.UserRepository;
import dinhgiang.dev.hungthinh.services.interfaces.IInvoiceService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import dinhgiang.dev.hungthinh.components.Auditable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Lớp cài đặt (Implementation) cho IInvoiceService.
 * Xử lý nghiệp vụ hóa đơn: tạo đơn lẻ, tạo hàng loạt và tính toán các loại phí dịch vụ.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Service
@RequiredArgsConstructor
public class InvoiceService implements IInvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final ApartmentRepository apartmentRepository;
    private final TableFeeRepository tableFeeRepository;
    private final ResidentRepository residentRepository;
    private final UtilityMeterReadingRepository utilityMeterReadingRepository;
    private final VehicleService vehicleService;
    private final SystemNotificationService systemNotificationService;
    private final TableElectricTierService tableElectricTierService;
    private final AccessControlService accessControlService;
    private final MeterReadingService meterReadingService;

    public PageResponse<InvoiceGetResponse> getAllInvoices(
            int page,
            int size,
            InvoiceStatus invoiceStatus,
            String sortBy,
            String direction,
            String keyword,
            Long apartmentId,
            String billingPeriod
    ) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Long scopedApartmentId = accessControlService.resolveApartmentScopeForResident(
                apartmentId,
                "Bạn không có quyền xem hóa đơn của căn hộ này"
        );

        Specification<Invoice> spec = Specification.allOf();
        if (invoiceStatus != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("invoiceStatus"), invoiceStatus));
        }
        if (scopedApartmentId != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("apartment").get("id"), scopedApartmentId));
        }
        if (billingPeriod != null && !billingPeriod.isBlank()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("billingPeriod"), billingPeriod.trim()));
        }
        if (keyword != null && !keyword.isBlank()) {
            Specification<Invoice> keywordSpec = getInvoiceSpecification(keyword);
            spec = spec.and(keywordSpec);
        }

        Page<Invoice> invoices = invoiceRepository.findAll(spec, pageRequest);

        List<InvoiceGetResponse> content = invoices.getContent().stream()
                .map(invoice -> InvoiceGetResponse.builder()
                        .invoiceId(invoice.getId())
                        .invoiceNumber(invoice.getInvoiceNumber())
                        .DueDate(invoice.getDueDate())
                        .billingPeriod(invoice.getBillingPeriod())
                        .createdAt(invoice.getCreatedAt())

                        .electricFee(invoice.getElectricFee())
                        .waterFee(invoice.getWaterFee())
                        .electricPreviousReading(invoice.getElectricPreviousReading())
                        .electricCurrentReading(invoice.getElectricCurrentReading())
                        .electricQuantity(invoice.getElectricQuantity())
                        .waterPreviousReading(invoice.getWaterPreviousReading())
                        .waterCurrentReading(invoice.getWaterCurrentReading())
                        .waterQuantity(invoice.getWaterQuantity())
                        .managementFee(invoice.getManagementFee())
                        .parkingFee(invoice.getParkingFee())
                        .otherFee(invoice.getOtherFee())
                        .totalAmount(invoice.getTotalAmount())

                        .descriptionOtherFee(invoice.getDescriptionOtherFee())

                        .invoiceStatus(invoice.getInvoiceStatus())

                        .creator(UserShortGetResponse.builder()
                                .userId(invoice.getCreator().getId())
                                .fullName(invoice.getCreator().getFullName())
                                .phoneNumber(invoice.getCreator().getPhoneNumber())
                                .build())
                        .apartment(ApartmentShortGetResponse.builder()
                                .id(invoice.getApartment().getId())
                                .apartmentNumber(invoice.getApartment().getApartmentNumber())
                                .floor(invoice.getApartment().getFloor())
                                .block(invoice.getApartment().getBlock())
                                .ownerId(invoice.getApartment().getOwnerId())
                                .build())
                        .payments(
                                invoice.getPayments() != null
                                    ? invoice.getPayments().stream()
                                        .map(payment -> PaymentGetResponse.builder()
                                                .paymentId(payment.getId())
                                                .amount(payment.getAmount())
                                                .paymentDateTime(payment.getPaymentDateTime())
                                                .paymentStatus(payment.getPaymentStatus())
                                                .paymentMethod(payment.getPaymentMethod().name())
                                                .transactionCode(payment.getTransactionNo())
                                                .payerName(payment.getPayerName())
                                                .payerPhoneNumber(resolvePayerPhoneNumber(payment))
                                                .build())
                                        .toList()
                                    : List.of()
                        )
                        .build())
                .toList();
        return new PageResponse<>(
                content,
                invoices.getNumber() + 1,
                invoices.getSize(),
                invoices.getTotalElements(),
                invoices.getTotalPages()
        );
    }

    private static @NonNull Specification<Invoice> getInvoiceSpecification(String keyword) {
        String likeKeyword = "%" + keyword + "%";

        Specification<Invoice> keywordSpec = (root, query, cb) ->
                cb.or(
                        cb.like(cb.lower(root.get("invoiceNumber")), likeKeyword.toLowerCase()),
                        cb.like(root.get("totalAmount").as(String.class), likeKeyword),
                        cb.like(cb.lower(root.join("apartment").get("apartmentNumber")), likeKeyword.toLowerCase())
                );

        try {
            LocalDate date = LocalDate.parse(keyword);
            keywordSpec = keywordSpec.or((root, query, cb) ->
                    cb.equal(root.get("DueDate"), date));
        } catch (DateTimeException ignore) {}
        return keywordSpec;
    }

    public InvoiceGetResponse getInvoiceById(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId).orElseThrow(() -> new UserMessageException("Hóa đơn không tồn tại"));
        accessControlService.assertResidentCanAccessApartment(
                invoice.getApartment(),
                "Bạn không có quyền xem hóa đơn này"
        );

        return InvoiceGetResponse.builder()
                .invoiceId(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .DueDate(invoice.getDueDate())
                .billingPeriod(invoice.getBillingPeriod())
                .createdAt(invoice.getCreatedAt())

                .electricFee(invoice.getElectricFee())
                .waterFee(invoice.getWaterFee())
                .electricPreviousReading(invoice.getElectricPreviousReading())
                .electricCurrentReading(invoice.getElectricCurrentReading())
                .electricQuantity(invoice.getElectricQuantity())
                .waterPreviousReading(invoice.getWaterPreviousReading())
                .waterCurrentReading(invoice.getWaterCurrentReading())
                .waterQuantity(invoice.getWaterQuantity())
                .managementFee(invoice.getManagementFee())
                .parkingFee(invoice.getParkingFee())
                .otherFee(invoice.getOtherFee())
                .totalAmount(invoice.getTotalAmount())

                .descriptionOtherFee(invoice.getDescriptionOtherFee())

                .invoiceStatus(invoice.getInvoiceStatus())

                .creator(UserShortGetResponse.builder()
                        .userId(invoice.getCreator().getId())
                        .fullName(invoice.getCreator().getFullName())
                        .phoneNumber(invoice.getCreator().getPhoneNumber())
                        .build())
                .apartment(ApartmentShortGetResponse.builder()
                        .id(invoice.getApartment().getId())
                        .apartmentNumber(invoice.getApartment().getApartmentNumber())
                        .floor(invoice.getApartment().getFloor())
                        .block(invoice.getApartment().getBlock())
                        .ownerId(invoice.getApartment().getOwnerId())
                        .build())
                .payments(
                        invoice.getPayments().stream()
                                .map(payment -> PaymentGetResponse.builder()
                                        .paymentId(payment.getId())
                                        .amount(payment.getAmount())
                                        .paymentDateTime(payment.getPaymentDateTime())
                                        .paymentStatus(payment.getPaymentStatus())
                                        .paymentMethod(payment.getPaymentMethod().name())
                                        .transactionCode(payment.getTransactionNo())
                                        .payerName(payment.getPayerName())
                                        .payerPhoneNumber(resolvePayerPhoneNumber(payment))
                                        .build())
                                .toList())
                .build();
    }

    private String resolvePayerPhoneNumber(Payment payment) {
        String username = payment.getPayerUsername();
        if (username == null || username.isBlank()) {
            return null;
        }

        return userRepository.findByUsername(username)
                .map(User::getPhoneNumber)
                .or(() -> residentRepository.findByUsername(username)
                        .map(resident -> resident.getPhoneNumber()))
                .orElse(null);
    }

    @Auditable(action = "CREATE", entityType = "INVOICE")
    public Long createInvoice(InvoiceCreateRequest apiRequest) {
        User creator = userRepository.findById(apiRequest.getCreatorId())
                .orElseThrow(() -> new UserMessageException("Người dùng không tồn tại"));
        Apartment apartment = apartmentRepository.findById(apiRequest.getApartmentId())
                .orElseThrow(() -> new UserMessageException("Căn hộ không tồn tại"));
        Invoice invoice = new Invoice();
        String billingPeriod = resolveBillingPeriod(apiRequest.getDueDate());
        assertBillingPeriodAvailable(apartment.getId(), billingPeriod, null, apartment.getApartmentNumber());

        // Tự sinh mã hóa đơn nếu không nhập
        String invoiceNumber = apiRequest.getInvoiceNumber();
        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            java.time.LocalDate now = java.time.LocalDate.now();
            String prefix = String.format("HD-%02d/%d-", now.getMonthValue(), now.getYear());
            long count = invoiceRepository.countByInvoiceNumberStartingWith(prefix);
            do {
                count++;
                invoiceNumber = prefix + String.format("%03d", count);
            } while (invoiceRepository.existsByInvoiceNumber(invoiceNumber));
        } else {
            if (invoiceRepository.existsByInvoiceNumber(invoiceNumber)) {
                throw new UserMessageException("Mã hóa đơn '" + invoiceNumber + "' đã tồn tại trong hệ thống");
            }
        }
        invoice.setInvoiceNumber(invoiceNumber);
        invoice.setDueDate(apiRequest.getDueDate());
        invoice.setBillingPeriod(billingPeriod);

        MeterReadingPreviewResponse meterSnapshot = resolveInvoiceMeterSnapshot(
                apartment,
                billingPeriod,
                apiRequest.getElectricPreviousReading(),
                apiRequest.getElectricCurrentReading(),
                apiRequest.getElectricQuantity(),
                apiRequest.getWaterPreviousReading(),
                apiRequest.getWaterCurrentReading(),
                apiRequest.getWaterQuantity()
        );
        applyMeterSnapshot(invoice, meterSnapshot);

        invoice.setElectricFee(apiRequest.getElectricFee());
        invoice.setWaterFee(apiRequest.getWaterFee());
        invoice.setManagementFee(apiRequest.getManagementFee());
        invoice.setParkingFee(apiRequest.getParkingFee());
        invoice.setOtherFee(apiRequest.getOtherFee());
        BigDecimal totalAmount = safe(apiRequest.getElectricFee())
                        .add(safe(apiRequest.getWaterFee()))
                        .add(safe(apiRequest.getManagementFee()))
                        .add(safe(apiRequest.getParkingFee()))
                        .add(safe(apiRequest.getOtherFee()));
        invoice.setTotalAmount(totalAmount);
        invoice.setDescriptionOtherFee(apiRequest.getDescriptionOtherFee());
        invoice.setInvoiceStatus(InvoiceStatus.UNPAID);
        invoice.setCreator(creator);
        invoice.setApartment(apartment);

        invoiceRepository.save(invoice);
        meterReadingService.recordInvoiceReading(
                apartment,
                invoice,
                billingPeriod,
                meterSnapshot,
                creator,
                resolveMeterReadingSource(apiRequest.getElectricMeterReadingSource(), apiRequest.getMeterReadingSource()),
                resolveMeterReadingSource(apiRequest.getWaterMeterReadingSource(), apiRequest.getMeterReadingSource())
        );

        // Thông báo hệ thống qua SystemNotificationService
        String formattedAmount = String.format("%,.0f", totalAmount);
        systemNotificationService.notifyApartment(
                "Hóa đơn mới: " + invoice.getInvoiceNumber(),
                "Bạn có hóa đơn mới với tổng tiền " + formattedAmount + " VNĐ. Hạn thanh toán: " + invoice.getDueDate(),
                apartment.getId()
        );

        return invoice.getId();
    }

    private BigDecimal safe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    @Auditable(action = "UPDATE", entityType = "INVOICE")
    public Long updateInvoice(Long invoiceId, InvoiceUpdateRequest apiRequest) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new UserMessageException("Hóa đơn không tồn tại"));
        if (apiRequest.getInvoiceNumber() != null) {
            invoice.setInvoiceNumber(apiRequest.getInvoiceNumber());
        }
        if (apiRequest.getDueDate() != null) {
            invoice.setDueDate(apiRequest.getDueDate());
            String billingPeriod = resolveBillingPeriod(apiRequest.getDueDate());
            assertBillingPeriodAvailable(
                    invoice.getApartment().getId(),
                    billingPeriod,
                    invoice.getId(),
                    invoice.getApartment().getApartmentNumber()
            );
            invoice.setBillingPeriod(billingPeriod);
        }
        if (apiRequest.getElectricFee() != null) {
            invoice.setElectricFee(apiRequest.getElectricFee());
        }
        if(apiRequest.getWaterFee() != null) {
            invoice.setWaterFee(apiRequest.getWaterFee());
        }
        if (apiRequest.getElectricPreviousReading() != null) {
            invoice.setElectricPreviousReading(apiRequest.getElectricPreviousReading());
        }
        if (apiRequest.getElectricCurrentReading() != null) {
            invoice.setElectricCurrentReading(apiRequest.getElectricCurrentReading());
        }
        if (apiRequest.getElectricQuantity() != null) {
            invoice.setElectricQuantity(apiRequest.getElectricQuantity());
        }
        if (apiRequest.getWaterPreviousReading() != null) {
            invoice.setWaterPreviousReading(apiRequest.getWaterPreviousReading());
        }
        if (apiRequest.getWaterCurrentReading() != null) {
            invoice.setWaterCurrentReading(apiRequest.getWaterCurrentReading());
        }
        if (apiRequest.getWaterQuantity() != null) {
            invoice.setWaterQuantity(apiRequest.getWaterQuantity());
        }
        if (apiRequest.getManagementFee() != null) {
            invoice.setManagementFee(apiRequest.getManagementFee());
        }
        if (apiRequest.getParkingFee() != null) {
            invoice.setParkingFee(apiRequest.getParkingFee());
        }
        if (apiRequest.getOtherFee() != null) {
            invoice.setOtherFee(apiRequest.getOtherFee());
        }
        BigDecimal totalAmount = safe(invoice.getElectricFee())
                .add(safe(invoice.getWaterFee()))
                .add(safe(invoice.getManagementFee()))
                .add(safe(invoice.getParkingFee()))
                .add(safe(invoice.getOtherFee()));
        invoice.setTotalAmount(totalAmount);
        if (apiRequest.getDescriptionOtherFee() != null
                && !apiRequest.getDescriptionOtherFee().isBlank()) {
            invoice.setDescriptionOtherFee(apiRequest.getDescriptionOtherFee());
        }
        if (apiRequest.getInvoiceStatus() != null) {
            invoice.setInvoiceStatus(apiRequest.getInvoiceStatus());
        }

        invoiceRepository.save(invoice);
        return invoice.getId();
    }

    @Auditable(action = "BATCH_CREATE", entityType = "INVOICE")
    @Transactional
    public List<Long> batchCreateInvoices(InvoiceBatchCreateRequest request) {
        User creator = userRepository.findById(request.getCreatorId())
                .orElseThrow(() -> new UserMessageException("Ng\u01b0\u1eddi d\u00f9ng kh\u00f4ng t\u1ed3n t\u1ea1i"));
        TableFee tableFee = tableFeeRepository.findById(request.getTableFeeId())
                .orElseThrow(() -> new UserMessageException("B\u1ea3ng ph\u00ed kh\u00f4ng t\u1ed3n t\u1ea1i"));
        String billingPeriod = resolveBillingPeriod(request.getDueDate());
        assertNoDuplicateApartmentIds(request.getApartmentIds());

        LocalDate now = LocalDate.now();
        String monthYear = String.format("%02d%d", now.getMonthValue(), now.getYear());
        long existingCount = invoiceRepository.count();

        List<Long> createdIds = new ArrayList<>();
        int seq = (int) existingCount + 1;

        for (Long apartmentId : request.getApartmentIds()) {
            Apartment apartment = apartmentRepository.findById(apartmentId)
                    .orElseThrow(() -> new UserMessageException("C\u0103n h\u1ed9 ID " + apartmentId + " kh\u00f4ng t\u1ed3n t\u1ea1i"));
            if (hasExistingInvoiceInBillingPeriod(apartmentId, billingPeriod, null)) {
                continue;
            }

            BigDecimal motorbikeParkingFee = feeValue(request.getMotorbikeParkingFee(), tableFee.getMotorbikeParkingFee());
            BigDecimal carParkingFee = feeValue(request.getCarParkingFee(), tableFee.getCarParkingFee());
            BigDecimal bicycleParkingFee = feeValue(request.getBicycleParkingFee(), tableFee.getBicycleParkingFee());
            BigDecimal electricMotorbikeParkingFee = feeValue(request.getElectricMotorbikeParkingFee(), tableFee.getElectricMotorbikeParkingFee());

            // T\u00ednh ph\u00ed g\u1eedi xe theo s\u1ed1 l\u01b0\u1ee3ng xe th\u1ef1c t\u1ebf
            BigDecimal parkingFee = safe(tableFee.getParkingFee());
            if (motorbikeParkingFee != null || carParkingFee != null || bicycleParkingFee != null || electricMotorbikeParkingFee != null) {
                parkingFee = vehicleService.calculateParkingFee(
                        apartmentId,
                        motorbikeParkingFee,
                        carParkingFee,
                        bicycleParkingFee,
                        electricMotorbikeParkingFee
                );
            }

            Boolean useTieredElectric = request.getUseTieredElectric() != null
                    ? request.getUseTieredElectric()
                    : Boolean.TRUE.equals(tableFee.getUseTieredElectric());
            MeterReadingPreviewResponse meterSnapshot = meterReadingService.previewNextReading(apartment, billingPeriod);
            BigDecimal electricFee = calculateBatchElectricFee(
                    meterSnapshot.getElectricQuantity(),
                    feeValue(request.getElectricFee(), tableFee.getElectricFee()),
                    useTieredElectric,
                    request.getDueDate()
            );
            BigDecimal waterFee = calculateBatchWaterFee(
                    meterSnapshot.getWaterQuantity(),
                    feeValue(request.getWaterFee(), tableFee.getWaterFee())
            );

            Invoice invoice = new Invoice();
            invoice.setInvoiceNumber(generateBatchInvoiceNumber(monthYear, seq++));
            invoice.setDueDate(request.getDueDate());
            invoice.setBillingPeriod(billingPeriod);
            applyMeterSnapshot(invoice, meterSnapshot);
            invoice.setElectricFee(electricFee);
            invoice.setWaterFee(waterFee);
            invoice.setManagementFee(safe(feeValue(request.getManagementFee(), tableFee.getManagementFee())));
            invoice.setParkingFee(parkingFee);
            invoice.setOtherFee(safe(feeValue(request.getOtherFee(), tableFee.getOtherFee())));
            invoice.setDescriptionOtherFee(request.getDescriptionOtherFee() != null
                    ? request.getDescriptionOtherFee()
                    : tableFee.getDescriptionOtherFee());

            BigDecimal totalAmount = safe(invoice.getElectricFee())
                    .add(safe(invoice.getWaterFee()))
                    .add(safe(invoice.getManagementFee()))
                    .add(safe(invoice.getParkingFee()))
                    .add(safe(invoice.getOtherFee()));
            invoice.setTotalAmount(totalAmount);
            invoice.setInvoiceStatus(InvoiceStatus.UNPAID);
            invoice.setCreator(creator);
            invoice.setApartment(apartment);

            invoiceRepository.save(invoice);
            meterReadingService.recordInvoiceReading(apartment, invoice, billingPeriod, meterSnapshot, creator, MeterReadingSource.MOCK_API);
            createdIds.add(invoice.getId());

            // Th\u00f4ng b\u00e1o h\u1ec7 th\u1ed1ng
            String formattedAmount = String.format("%,.0f", totalAmount);
            systemNotificationService.notifyApartment(
                    "H\u00f3a \u0111\u01a1n m\u1edbi: " + invoice.getInvoiceNumber(),
                    "B\u1ea1n c\u00f3 h\u00f3a \u0111\u01a1n m\u1edbi v\u1edbi t\u1ed5ng ti\u1ec1n " + formattedAmount + " VN\u0110. H\u1ea1n thanh to\u00e1n: " + invoice.getDueDate(),
                    apartment.getId()
            );
        }
        return createdIds;
    }

    private String generateBatchInvoiceNumber(String monthYear, int seq) {
        String invoiceNumber;
        do {
            invoiceNumber = "HD-" + monthYear + "-" + String.format("%03d", seq++);
        } while (invoiceRepository.existsByInvoiceNumber(invoiceNumber));
        return invoiceNumber;
    }

    private String resolveBillingPeriod(LocalDate dueDate) {
        if (dueDate == null) {
            throw new UserMessageException("Hạn thanh toán không được để trống");
        }
        return YearMonth.from(dueDate).toString();
    }

    private void assertNoDuplicateApartmentIds(List<Long> apartmentIds) {
        if (apartmentIds == null || apartmentIds.isEmpty()) {
            throw new UserMessageException("Danh sách căn hộ không được để trống");
        }

        Set<Long> seenApartmentIds = new HashSet<>();
        for (Long apartmentId : apartmentIds) {
            if (apartmentId == null) {
                throw new UserMessageException("Danh sách căn hộ chứa ID không hợp lệ");
            }
            if (!seenApartmentIds.add(apartmentId)) {
                throw new UserMessageException("Danh sách căn hộ bị trùng ID " + apartmentId);
            }
        }
    }

    private void assertBillingPeriodAvailable(Long apartmentId, String billingPeriod, Long currentInvoiceId, String apartmentNumber) {
        if (hasExistingInvoiceInBillingPeriod(apartmentId, billingPeriod, currentInvoiceId)) {
            String apartmentLabel = apartmentNumber != null ? apartmentNumber : String.valueOf(apartmentId);
            throw new UserMessageException("Căn hộ " + apartmentLabel + " đã có hóa đơn kỳ " + billingPeriod);
        }
    }

    private boolean hasExistingInvoiceInBillingPeriod(Long apartmentId, String billingPeriod, Long currentInvoiceId) {
        YearMonth period = YearMonth.parse(billingPeriod);
        return invoiceRepository.countExistingInBillingPeriod(
                apartmentId,
                billingPeriod,
                period.atDay(1),
                period.atEndOfMonth(),
                currentInvoiceId
        ) > 0;
    }

    private BigDecimal feeValue(BigDecimal requestValue, BigDecimal tableValue) {
        return requestValue != null ? requestValue : tableValue;
    }

    private MeterReadingSource resolveMeterReadingSource(MeterReadingSource specificSource, MeterReadingSource fallbackSource) {
        if (specificSource != null) {
            return specificSource;
        }
        return fallbackSource != null ? fallbackSource : MeterReadingSource.MOCK_API;
    }

    private BigDecimal calculateBatchElectricFee(
            BigDecimal electricQuantity,
            BigDecimal electricUnitFee,
            Boolean useTieredElectric,
            LocalDate dueDate
    ) {
        Long kwhConsumed = safe(electricQuantity).setScale(0, RoundingMode.HALF_UP).longValue();
        if (kwhConsumed <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal electricFee;
        if (Boolean.TRUE.equals(useTieredElectric)) {
            LocalDate endDate = dueDate != null ? dueDate : LocalDate.now().plusMonths(1);
            electricFee = tableElectricTierService.calculatorElectricFeeWithTier(kwhConsumed, LocalDate.now(), endDate, 1);
        } else {
            electricFee = BigDecimal.valueOf(kwhConsumed).multiply(safe(electricUnitFee));
        }

        return electricFee.multiply(BigDecimal.valueOf(1.08)).setScale(0, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateBatchWaterFee(BigDecimal waterQuantity, BigDecimal waterUnitFee) {
        return safe(waterQuantity)
                .multiply(safe(waterUnitFee))
                .multiply(BigDecimal.valueOf(1.15))
                .setScale(0, RoundingMode.HALF_UP);
    }

    private MeterReadingPreviewResponse resolveInvoiceMeterSnapshot(
            Apartment apartment,
            String billingPeriod,
            BigDecimal electricPreviousReading,
            BigDecimal electricCurrentReading,
            BigDecimal electricQuantity,
            BigDecimal waterPreviousReading,
            BigDecimal waterCurrentReading,
            BigDecimal waterQuantity
    ) {
        MeterReadingPreviewResponse fallback = meterReadingService.previewNextReading(apartment, billingPeriod);

        BigDecimal resolvedElectricPrevious = firstNonNull(electricPreviousReading, fallback.getElectricPreviousReading());
        BigDecimal resolvedElectricQuantity = firstNonNull(electricQuantity, fallback.getElectricQuantity());
        BigDecimal resolvedElectricCurrent = firstNonNull(electricCurrentReading, fallback.getElectricCurrentReading());
        if (electricQuantity == null && electricCurrentReading != null) {
            resolvedElectricQuantity = electricCurrentReading.subtract(resolvedElectricPrevious);
        }
        if (electricCurrentReading == null && electricQuantity != null) {
            resolvedElectricCurrent = resolvedElectricPrevious.add(electricQuantity);
        }
        assertMeterDeltaValid(resolvedElectricPrevious, resolvedElectricCurrent, "Chỉ số điện cuối kỳ không được nhỏ hơn đầu kỳ");

        BigDecimal resolvedWaterPrevious = firstNonNull(waterPreviousReading, fallback.getWaterPreviousReading());
        BigDecimal resolvedWaterQuantity = firstNonNull(waterQuantity, fallback.getWaterQuantity());
        BigDecimal resolvedWaterCurrent = firstNonNull(waterCurrentReading, fallback.getWaterCurrentReading());
        if (waterQuantity == null && waterCurrentReading != null) {
            resolvedWaterQuantity = waterCurrentReading.subtract(resolvedWaterPrevious);
        }
        if (waterCurrentReading == null && waterQuantity != null) {
            resolvedWaterCurrent = resolvedWaterPrevious.add(waterQuantity);
        }
        assertMeterDeltaValid(resolvedWaterPrevious, resolvedWaterCurrent, "Chỉ số nước cuối kỳ không được nhỏ hơn đầu kỳ");

        return MeterReadingPreviewResponse.builder()
                .apartmentId(apartment.getId())
                .apartmentNumber(apartment.getApartmentNumber())
                .billingPeriod(billingPeriod)
                .electricPreviousReading(resolvedElectricPrevious)
                .electricQuantity(resolvedElectricQuantity)
                .electricCurrentReading(resolvedElectricCurrent)
                .waterPreviousReading(resolvedWaterPrevious)
                .waterQuantity(resolvedWaterQuantity)
                .waterCurrentReading(resolvedWaterCurrent)
                .build();
    }

    private void applyMeterSnapshot(Invoice invoice, MeterReadingPreviewResponse meterSnapshot) {
        invoice.setElectricPreviousReading(meterSnapshot.getElectricPreviousReading());
        invoice.setElectricCurrentReading(meterSnapshot.getElectricCurrentReading());
        invoice.setElectricQuantity(meterSnapshot.getElectricQuantity());
        invoice.setWaterPreviousReading(meterSnapshot.getWaterPreviousReading());
        invoice.setWaterCurrentReading(meterSnapshot.getWaterCurrentReading());
        invoice.setWaterQuantity(meterSnapshot.getWaterQuantity());
    }

    private BigDecimal firstNonNull(BigDecimal value, BigDecimal fallback) {
        return value != null ? value : safe(fallback);
    }

    private void assertMeterDeltaValid(BigDecimal previous, BigDecimal current, String message) {
        if (previous != null && current != null && current.compareTo(previous) < 0) {
            throw new UserMessageException(message);
        }
    }

    @Auditable(action = "DELETE", entityType = "INVOICE")
    @Transactional
    public Void deleteInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new UserMessageException("Hóa đơn không tồn tại"));
        if (invoice.getInvoiceStatus() == InvoiceStatus.PAID) {
            throw new UserMessageException("Không thể xóa hóa đơn đã thanh toán");
        }
        utilityMeterReadingRepository.unlinkInvoice(invoiceId);
        invoiceRepository.delete(invoice);
        return null;
    }
}
