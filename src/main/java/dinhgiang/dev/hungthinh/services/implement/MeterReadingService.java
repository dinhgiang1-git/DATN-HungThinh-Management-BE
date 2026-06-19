package dinhgiang.dev.hungthinh.services.implement;

import dinhgiang.dev.hungthinh.exceptions.UserMessageException;
import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentShortGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.evns.EvnBillResponse;
import dinhgiang.dev.hungthinh.models.dtos.meterreadings.MeterReadingGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.meterreadings.MeterReadingPreviewResponse;
import dinhgiang.dev.hungthinh.models.dtos.users.UserShortGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.water.WaterBillResponse;
import dinhgiang.dev.hungthinh.models.entities.bases.Apartment;
import dinhgiang.dev.hungthinh.models.entities.bases.Invoice;
import dinhgiang.dev.hungthinh.models.entities.bases.Resident;
import dinhgiang.dev.hungthinh.models.entities.bases.User;
import dinhgiang.dev.hungthinh.models.entities.bases.UtilityMeterReading;
import dinhgiang.dev.hungthinh.models.entities.enums.MeterReadingSource;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import dinhgiang.dev.hungthinh.repositories.ApartmentRepository;
import dinhgiang.dev.hungthinh.repositories.ResidentRepository;
import dinhgiang.dev.hungthinh.repositories.UserRepository;
import dinhgiang.dev.hungthinh.repositories.UtilityMeterReadingRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Lớp dịch vụ quản lý ghi chỉ số đồng hồ (Meter Reading).
 * Xử lý nhập chỉ số điện, nước định kỳ hoặc từ nguồn API giả lập.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Service
@RequiredArgsConstructor
public class MeterReadingService {
    private static final List<String> ALLOWED_IMAGE_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024;

    private final UtilityMeterReadingRepository meterReadingRepository;
    private final ApartmentRepository apartmentRepository;
    private final UserRepository userRepository;
    private final ResidentRepository residentRepository;
    private final EvnMockService evnMockService;
    private final WaterMockService waterMockService;

    @Value("${meter-reading.upload-dir:uploads/meter-readings}")
    private String uploadDir;

    public PageResponse<MeterReadingGetResponse> getAll(int page, int size, Long apartmentId, String billingPeriod) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());

        Specification<UtilityMeterReading> spec = Specification.allOf();
        if (apartmentId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("apartment").get("id"), apartmentId));
        }
        if (billingPeriod != null && !billingPeriod.isBlank()) {
            String normalizedBillingPeriod = normalizeBillingPeriod(billingPeriod);
            spec = spec.and((root, query, cb) -> cb.equal(root.get("billingPeriod"), normalizedBillingPeriod));
        }

        Page<UtilityMeterReading> readings = meterReadingRepository.findAll(spec, pageRequest);
        return new PageResponse<>(
                readings.getContent().stream().map(this::toResponse).toList(),
                readings.getNumber() + 1,
                readings.getSize(),
                readings.getTotalElements(),
                readings.getTotalPages()
        );
    }

    public MeterReadingPreviewResponse previewNextReading(Long apartmentId) {
        return previewNextReading(apartmentId, null);
    }

    public MeterReadingPreviewResponse previewNextReading(Long apartmentId, String billingPeriod) {
        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new UserMessageException("Căn hộ không tồn tại"));
        return previewNextReading(apartment, billingPeriod);
    }

    public MeterReadingPreviewResponse previewNextReading(Apartment apartment) {
        return previewNextReading(apartment, null);
    }

    public MeterReadingPreviewResponse previewNextReading(Apartment apartment, String billingPeriod) {
        String targetBillingPeriod = normalizeBillingPeriod(billingPeriod);
        Long residentId = resolveResidentId(apartment);
        BigDecimal electricPrevious = latestElectricReadingBefore(apartment.getId(), targetBillingPeriod);
        BigDecimal waterPrevious = latestWaterReadingBefore(apartment.getId(), targetBillingPeriod);

        BigDecimal electricQuantity = BigDecimal.ZERO;
        BigDecimal waterQuantity = BigDecimal.ZERO;

        java.util.Random random = new java.util.Random();
        if (residentId != null) {
            try {
                EvnBillResponse evnBill = evnMockService.getBillByResidentId(residentId, false);
                electricQuantity = toBigDecimal(evnBill.getKwhConsumed());
            } catch (Exception e) {
                electricQuantity = BigDecimal.valueOf(125 + random.nextInt(500));
            }

            try {
                WaterBillResponse waterBill = waterMockService.getBillByResidentId(residentId, false);
                waterQuantity = toBigDecimal(waterBill.getCubicMeterConsumed());
            } catch (Exception e) {
                waterQuantity = BigDecimal.valueOf(10 + random.nextInt(40));
            }
        } else {
            electricQuantity = BigDecimal.valueOf(125 + random.nextInt(500));
            waterQuantity = BigDecimal.valueOf(10 + random.nextInt(40));
        }

        return MeterReadingPreviewResponse.builder()
                .apartmentId(apartment.getId())
                .apartmentNumber(apartment.getApartmentNumber())
                .billingPeriod(targetBillingPeriod)
                .electricPreviousReading(electricPrevious)
                .electricQuantity(electricQuantity)
                .electricCurrentReading(electricPrevious.add(electricQuantity))
                .waterPreviousReading(waterPrevious)
                .waterQuantity(waterQuantity)
                .waterCurrentReading(waterPrevious.add(waterQuantity))
                .build();
    }

    @Transactional
    public MeterReadingGetResponse recordManualReading(
            Long apartmentId,
            String billingPeriod,
            BigDecimal electricCurrentReading,
            BigDecimal waterCurrentReading,
            String note,
            MultipartFile evidence
    ) {
        return recordManualReading(apartmentId, billingPeriod, electricCurrentReading, waterCurrentReading, note, evidence, null, null);
    }

    @Transactional
    public MeterReadingGetResponse recordManualReading(
            Long apartmentId,
            String billingPeriod,
            BigDecimal electricCurrentReading,
            BigDecimal waterCurrentReading,
            String note,
            MultipartFile evidence,
            MultipartFile electricEvidence,
            MultipartFile waterEvidence
    ) {
        if (electricCurrentReading == null && waterCurrentReading == null) {
            throw new UserMessageException("Vui lòng nhập chỉ số điện hoặc nước");
        }

        Apartment apartment = apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new UserMessageException("Căn hộ không tồn tại"));
        User recordedBy = currentUser();
        String normalizedBillingPeriod = normalizeBillingPeriod(billingPeriod);
        assertNotAlreadyRecorded(apartmentId, normalizedBillingPeriod, electricCurrentReading != null, waterCurrentReading != null);

        BigDecimal electricPrevious = latestElectricReadingBefore(apartmentId, normalizedBillingPeriod);
        BigDecimal waterPrevious = latestWaterReadingBefore(apartmentId, normalizedBillingPeriod);

        UtilityMeterReading reading = new UtilityMeterReading();
        reading.setApartment(apartment);
        reading.setRecordedBy(recordedBy);
        reading.setBillingPeriod(normalizedBillingPeriod);
        reading.setSource(MeterReadingSource.MANUAL);
        reading.setNote(note);

        if (electricCurrentReading != null) {
            assertNotLowerThanPrevious(electricCurrentReading, electricPrevious, "Chỉ số điện mới không được nhỏ hơn chỉ số trước đó");
            reading.setElectricPreviousReading(electricPrevious);
            reading.setElectricCurrentReading(electricCurrentReading);
            reading.setElectricQuantity(electricCurrentReading.subtract(electricPrevious));
        }

        if (waterCurrentReading != null) {
            assertNotLowerThanPrevious(waterCurrentReading, waterPrevious, "Chỉ số nước mới không được nhỏ hơn chỉ số trước đó");
            reading.setWaterPreviousReading(waterPrevious);
            reading.setWaterCurrentReading(waterCurrentReading);
            reading.setWaterQuantity(waterCurrentReading.subtract(waterPrevious));
        }

        if (evidence != null && !evidence.isEmpty()) {
            StoredEvidence storedEvidence = storeEvidence(evidence);
            reading.setEvidenceFilePath(storedEvidence.getStoredFileName());
            reading.setEvidenceOriginalFileName(evidence.getOriginalFilename());
            reading.setEvidenceContentType(storedEvidence.getContentType());
        }

        if (electricEvidence != null && !electricEvidence.isEmpty()) {
            StoredEvidence storedEvidence = storeEvidence(electricEvidence);
            reading.setElectricEvidenceFilePath(storedEvidence.getStoredFileName());
            reading.setElectricEvidenceOriginalFileName(electricEvidence.getOriginalFilename());
            reading.setElectricEvidenceContentType(storedEvidence.getContentType());
        }

        if (waterEvidence != null && !waterEvidence.isEmpty()) {
            StoredEvidence storedEvidence = storeEvidence(waterEvidence);
            reading.setWaterEvidenceFilePath(storedEvidence.getStoredFileName());
            reading.setWaterEvidenceOriginalFileName(waterEvidence.getOriginalFilename());
            reading.setWaterEvidenceContentType(storedEvidence.getContentType());
        }

        return toResponse(meterReadingRepository.save(reading));
    }

    private void assertNotAlreadyRecorded(Long apartmentId, String billingPeriod, boolean hasElectric, boolean hasWater) {
        if ((hasElectric || hasWater) && meterReadingRepository.existsByApartment_IdAndBillingPeriod(apartmentId, billingPeriod)) {
            throw new UserMessageException("Căn hộ này đã ghi chỉ số điện nước trong kỳ " + billingPeriod);
        }
    }

    @Transactional
    public void recordInvoiceReading(
            Apartment apartment,
            Invoice invoice,
            String billingPeriod,
            MeterReadingPreviewResponse readingSnapshot,
            User recordedBy,
            MeterReadingSource source
    ) {
        recordInvoiceReading(apartment, invoice, billingPeriod, readingSnapshot, recordedBy, source, source);
    }

    @Transactional
    public void recordInvoiceReading(
            Apartment apartment,
            Invoice invoice,
            String billingPeriod,
            MeterReadingPreviewResponse readingSnapshot,
            User recordedBy,
            MeterReadingSource electricSource,
            MeterReadingSource waterSource
    ) {
        boolean hasElectric = readingSnapshot.getElectricCurrentReading() != null;
        boolean hasWater = readingSnapshot.getWaterCurrentReading() != null;
        if (!hasElectric && !hasWater) {
            return;
        }

        MeterReadingSource resolvedElectricSource = electricSource != null ? electricSource : MeterReadingSource.MOCK_API;
        MeterReadingSource resolvedWaterSource = waterSource != null ? waterSource : MeterReadingSource.MOCK_API;

        if (hasElectric && hasWater && resolvedElectricSource != resolvedWaterSource) {
            UtilityMeterReading electricReading = createInvoiceReading(apartment, invoice, billingPeriod, recordedBy, resolvedElectricSource);
            electricReading.setElectricPreviousReading(readingSnapshot.getElectricPreviousReading());
            electricReading.setElectricCurrentReading(readingSnapshot.getElectricCurrentReading());
            electricReading.setElectricQuantity(readingSnapshot.getElectricQuantity());
            meterReadingRepository.save(electricReading);

            UtilityMeterReading waterReading = createInvoiceReading(apartment, invoice, billingPeriod, recordedBy, resolvedWaterSource);
            waterReading.setWaterPreviousReading(readingSnapshot.getWaterPreviousReading());
            waterReading.setWaterCurrentReading(readingSnapshot.getWaterCurrentReading());
            waterReading.setWaterQuantity(readingSnapshot.getWaterQuantity());
            meterReadingRepository.save(waterReading);
            return;
        }

        MeterReadingSource source = hasElectric ? resolvedElectricSource : resolvedWaterSource;
        UtilityMeterReading reading = createInvoiceReading(apartment, invoice, billingPeriod, recordedBy, source);
        if (hasElectric) {
            reading.setElectricPreviousReading(readingSnapshot.getElectricPreviousReading());
            reading.setElectricCurrentReading(readingSnapshot.getElectricCurrentReading());
            reading.setElectricQuantity(readingSnapshot.getElectricQuantity());
        }
        if (hasWater) {
            reading.setWaterPreviousReading(readingSnapshot.getWaterPreviousReading());
            reading.setWaterCurrentReading(readingSnapshot.getWaterCurrentReading());
            reading.setWaterQuantity(readingSnapshot.getWaterQuantity());
        }
        meterReadingRepository.save(reading);
    }

    private UtilityMeterReading createInvoiceReading(
            Apartment apartment,
            Invoice invoice,
            String billingPeriod,
            User recordedBy,
            MeterReadingSource source
    ) {
        UtilityMeterReading reading = new UtilityMeterReading();
        reading.setApartment(apartment);
        reading.setInvoice(invoice);
        reading.setRecordedBy(recordedBy);
        reading.setBillingPeriod(normalizeBillingPeriod(billingPeriod));
        reading.setSource(source);
        return reading;
    }

    public Resource loadEvidenceAsResource(Long meterReadingId) {
        UtilityMeterReading reading = meterReadingRepository.findById(meterReadingId)
                .orElseThrow(() -> new UserMessageException("Bản ghi chỉ số không tồn tại"));
        return loadEvidenceFile(reading, reading.getEvidenceFilePath());
    }

    public Resource loadEvidenceAsResource(Long meterReadingId, String type) {
        UtilityMeterReading reading = meterReadingRepository.findById(meterReadingId)
                .orElseThrow(() -> new UserMessageException("Bản ghi chỉ số không tồn tại"));
        String normalizedType = type == null ? "" : type.trim().toLowerCase(Locale.ROOT);
        String evidenceFilePath = switch (normalizedType) {
            case "electric" -> firstNonBlank(reading.getElectricEvidenceFilePath(), reading.getEvidenceFilePath());
            case "water" -> firstNonBlank(reading.getWaterEvidenceFilePath(), reading.getEvidenceFilePath());
            default -> throw new UserMessageException("Loại ảnh kiểm chứng không hợp lệ");
        };
        return loadEvidenceFile(reading, evidenceFilePath);
    }

    private Resource loadEvidenceFile(UtilityMeterReading reading, String evidenceFilePath) {
        if (evidenceFilePath == null || evidenceFilePath.isBlank()) {
            throw new UserMessageException("Bản ghi này chưa có ảnh kiểm chứng");
        }
        try {
            Path filePath = evidenceStoragePath().resolve(evidenceFilePath).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new UserMessageException("Ảnh kiểm chứng không tồn tại");
        } catch (MalformedURLException ex) {
            throw new UserMessageException("Ảnh kiểm chứng không tồn tại");
        }
    }

    public UtilityMeterReading getReading(Long meterReadingId) {
        return meterReadingRepository.findById(meterReadingId)
                .orElseThrow(() -> new UserMessageException("Bản ghi chỉ số không tồn tại"));
    }

    private BigDecimal latestElectricReadingBefore(Long apartmentId, String billingPeriod) {
        return meterReadingRepository.findTopByApartment_IdAndBillingPeriodLessThanAndElectricCurrentReadingIsNotNullOrderByBillingPeriodDescIdDesc(apartmentId, billingPeriod)
                .map(UtilityMeterReading::getElectricCurrentReading)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal latestWaterReadingBefore(Long apartmentId, String billingPeriod) {
        return meterReadingRepository.findTopByApartment_IdAndBillingPeriodLessThanAndWaterCurrentReadingIsNotNullOrderByBillingPeriodDescIdDesc(apartmentId, billingPeriod)
                .map(UtilityMeterReading::getWaterCurrentReading)
                .orElse(BigDecimal.ZERO);
    }

    private Long resolveResidentId(Apartment apartment) {
        if (apartment.getOwnerId() != null) {
            return apartment.getOwnerId();
        }
        return residentRepository.findByApartment_Id(apartment.getId()).stream()
                .findFirst()
                .map(Resident::getId)
                .orElse(null);
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserMessageException("Bạn chưa đăng nhập");
        }
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UserMessageException("Người dùng không tồn tại"));
    }

    private void assertNotLowerThanPrevious(BigDecimal current, BigDecimal previous, String message) {
        if (current.compareTo(previous) < 0) {
            throw new UserMessageException(message + " (" + previous.stripTrailingZeros().toPlainString() + ")");
        }
    }

    private String normalizeBillingPeriod(String billingPeriod) {
        if (billingPeriod == null || billingPeriod.isBlank()) {
            return YearMonth.now().toString();
        }
        try {
            return YearMonth.parse(billingPeriod.trim()).toString();
        } catch (Exception ex) {
            throw new UserMessageException("Kỳ ghi chỉ số phải có định dạng yyyy-MM");
        }
    }

    private BigDecimal toBigDecimal(Integer value) {
        return value != null ? BigDecimal.valueOf(value.longValue()) : BigDecimal.ZERO;
    }

    private MeterReadingGetResponse toResponse(UtilityMeterReading reading) {
        Apartment apartment = reading.getApartment();
        User recordedBy = reading.getRecordedBy();

        return MeterReadingGetResponse.builder()
                .meterReadingId(reading.getId())
                .billingPeriod(reading.getBillingPeriod())
                .recordedAt(reading.getCreatedAt())
                .electricPreviousReading(reading.getElectricPreviousReading())
                .electricCurrentReading(reading.getElectricCurrentReading())
                .electricQuantity(reading.getElectricQuantity())
                .waterPreviousReading(reading.getWaterPreviousReading())
                .waterCurrentReading(reading.getWaterCurrentReading())
                .waterQuantity(reading.getWaterQuantity())
                .source(reading.getSource())
                .evidenceOriginalFileName(reading.getEvidenceOriginalFileName())
                .evidenceUrl(reading.getEvidenceFilePath() != null ? "/api/v1/meter-readings/" + reading.getId() + "/evidence" : null)
                .electricEvidenceOriginalFileName(firstNonBlank(reading.getElectricEvidenceOriginalFileName(), reading.getEvidenceOriginalFileName()))
                .electricEvidenceUrl(hasEvidenceForType(reading, "electric") ? "/api/v1/meter-readings/" + reading.getId() + "/evidence/electric" : null)
                .waterEvidenceOriginalFileName(firstNonBlank(reading.getWaterEvidenceOriginalFileName(), reading.getEvidenceOriginalFileName()))
                .waterEvidenceUrl(hasEvidenceForType(reading, "water") ? "/api/v1/meter-readings/" + reading.getId() + "/evidence/water" : null)
                .note(reading.getNote())
                .invoiceId(reading.getInvoice() != null ? reading.getInvoice().getId() : null)
                .apartment(ApartmentShortGetResponse.builder()
                        .id(apartment.getId())
                        .apartmentNumber(apartment.getApartmentNumber())
                        .floor(apartment.getFloor())
                        .block(apartment.getBlock())
                        .ownerId(apartment.getOwnerId())
                        .build())
                .recordedBy(recordedBy != null ? UserShortGetResponse.builder()
                        .userId(recordedBy.getId())
                        .fullName(recordedBy.getFullName())
                        .phoneNumber(recordedBy.getPhoneNumber())
                        .build() : null)
                .build();
    }

    private boolean hasEvidenceForType(UtilityMeterReading reading, String type) {
        if ("electric".equals(type)) {
            return hasText(reading.getElectricEvidenceFilePath())
                    || (hasText(reading.getEvidenceFilePath()) && reading.getElectricCurrentReading() != null);
        }
        if ("water".equals(type)) {
            return hasText(reading.getWaterEvidenceFilePath())
                    || (hasText(reading.getEvidenceFilePath()) && reading.getWaterCurrentReading() != null);
        }
        return false;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }

    private StoredEvidence storeEvidence(MultipartFile file) {
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new UserMessageException("Ảnh kiểm chứng vượt quá kích thước tối đa 10MB");
        }

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new UserMessageException("Tên ảnh không hợp lệ");
        }

        String extension = getFileExtension(originalFileName).toLowerCase(Locale.ROOT);
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new UserMessageException("Chỉ cho phép upload ảnh .jpg, .jpeg, .png, .webp");
        }

        String storedFileName = UUID.randomUUID() + "." + extension;
        String contentType = file.getContentType() != null ? file.getContentType() : "image/" + extension;

        try {
            Path storagePath = evidenceStoragePath();
            Files.createDirectories(storagePath);
            Files.copy(file.getInputStream(), storagePath.resolve(storedFileName), StandardCopyOption.REPLACE_EXISTING);
            return new StoredEvidence(storedFileName, contentType);
        } catch (IOException ex) {
            throw new RuntimeException("Không thể lưu ảnh kiểm chứng: " + originalFileName, ex);
        }
    }

    private Path evidenceStoragePath() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0) {
            return "";
        }
        return fileName.substring(lastDot + 1);
    }

    @Getter
    @RequiredArgsConstructor
    private static class StoredEvidence {
        private final String storedFileName;
        private final String contentType;
    }
}
