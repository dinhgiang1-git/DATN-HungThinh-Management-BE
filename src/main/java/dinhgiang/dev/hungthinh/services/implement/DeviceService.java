package dinhgiang.dev.hungthinh.services.implement;

import dinhgiang.dev.hungthinh.exceptions.UserMessageException;
import dinhgiang.dev.hungthinh.models.dtos.devices.DeviceCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.devices.DeviceGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.devices.DeviceUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.bases.Device;
import dinhgiang.dev.hungthinh.models.entities.bases.Apartment;
import dinhgiang.dev.hungthinh.models.entities.enums.DeviceStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.DeviceType;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import dinhgiang.dev.hungthinh.repositories.DeviceRepository;
import dinhgiang.dev.hungthinh.repositories.ApartmentRepository;
import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentShortGetResponse;
import dinhgiang.dev.hungthinh.services.interfaces.IDeviceService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;

/**
 * Lớp cài đặt (Implementation) cho IDeviceService.
 * Xử lý các nghiệp vụ quản lý thiết bị: tìm kiếm, thêm, sửa, xóa thiết bị chung/riêng.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Service
@RequiredArgsConstructor
public class DeviceService implements IDeviceService {
    private final DeviceRepository deviceRepository;
    private final ApartmentRepository apartmentRepository;

    public PageResponse<DeviceGetResponse> getAllDevices(
            int page,
            int size,
            DeviceStatus deviceStatus,
            DeviceType deviceType,
            String sortBy,
            String direction,
            String keyword,
            Long apartmentId
    ) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by((sortBy)).descending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Specification<Device> spec = Specification.allOf();
        if (deviceStatus != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("deviceStatus"), deviceStatus));
        }
        if (deviceType != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("deviceType"), deviceType));
        }
        if (apartmentId != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("apartment").get("id"), apartmentId));
        }
        if (keyword != null && !keyword.isBlank()) {
            Specification<Device> keywordSpec = getDeviceSpecification(keyword);
            spec = spec.and(keywordSpec);
        }

        Page<Device> devices = deviceRepository.findAll(spec, pageRequest);

        List<DeviceGetResponse> responses = devices.getContent().stream()
                .map( device -> DeviceGetResponse.builder()
                        .id(device.getId())
                        .deviceName(device.getDeviceName())
                        .installationDate(device.getInstallationDate())
                        .location(device.getLocation())
                        .maintenanceCycleDay(device.getMaintenanceCycleDay())
                        .deviceStatus(device.getDeviceStatus())
                        .deviceType(device.getDeviceType())
                        .apartment(
                                device.getApartment() != null
                                        ? ApartmentShortGetResponse.builder()
                                        .id(device.getApartment().getId())
                                        .apartmentNumber(device.getApartment().getApartmentNumber())
                                        .block(device.getApartment().getBlock())
                                        .floor(device.getApartment().getFloor())
                                        .build()
                                        : null
                        )
                        .build())
                .toList();
        return new PageResponse<>(
                responses,
                devices.getNumber() + 1,
                devices.getSize(),
                devices.getTotalElements(),
                devices.getTotalPages());
    }

    private static @NonNull Specification<Device> getDeviceSpecification(String keyword) {
        String likeKeyword = "%" + keyword.toLowerCase() + "%";
        Specification<Device> keywordSpec = (root, query, cb) ->
                cb.or(
                        cb.like(cb.lower(root.get("deviceName")), likeKeyword),
                        cb.like(cb.lower(root.get("location")), likeKeyword)
                );
        try {
            LocalDate date = LocalDate.parse(keyword);
            keywordSpec = keywordSpec.or((root, query, cb) ->
                    cb.equal(root.get("installationDate"), date));
        } catch (DateTimeException ignore) {}

        try {
            Integer integer = (Integer) Integer.parseInt(keyword);
            keywordSpec = keywordSpec.or((root, query, cb) ->
                    cb.equal(root.get("maintenanceCycleDay"), integer));
        } catch (NumberFormatException ignore) {}
        return keywordSpec;
    }

    public DeviceGetResponse getDeviceById(Long deviceId) {
        Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new UserMessageException("Thiết bị không tồn tại"));
        return DeviceGetResponse.builder()
                .id(device.getId())
                .deviceName(device.getDeviceName())
                .installationDate(device.getInstallationDate())
                .location(device.getLocation())
                .maintenanceCycleDay(device.getMaintenanceCycleDay())
                .deviceStatus(device.getDeviceStatus())
                .deviceType(device.getDeviceType())
                .apartment(
                        device.getApartment() != null
                                ? ApartmentShortGetResponse.builder()
                                .id(device.getApartment().getId())
                                .apartmentNumber(device.getApartment().getApartmentNumber())
                                .block(device.getApartment().getBlock())
                                .floor(device.getApartment().getFloor())
                                .build()
                                : null
                )
                .build();
    }

    public Long createDevice(DeviceCreateRequest apiRequest) {
        Device device = new Device();
        device.setDeviceName(apiRequest.getDeviceName());
        device.setInstallationDate(apiRequest.getInstallationDate());
        device.setLocation(apiRequest.getLocation());
        device.setMaintenanceCycleDay(apiRequest.getMaintenanceCycleDay());
        device.setDeviceStatus(apiRequest.getDeviceStatus());
        device.setDeviceType(apiRequest.getDeviceType() != null ? apiRequest.getDeviceType() : DeviceType.COMMON);

        if (apiRequest.getApartmentId() != null) {
            Apartment apartment = apartmentRepository.findById(apiRequest.getApartmentId())
                    .orElseThrow(() -> new UserMessageException("Căn hộ không tồn tại"));
            device.setApartment(apartment);
            
            // If location is null/empty for APARTMENT device, set it to apartment info
            if (device.getLocation() == null || device.getLocation().isBlank()) {
                device.setLocation("Căn hộ " + apartment.getApartmentNumber() + " (Block " + apartment.getBlock() + ")");
            }
        } else if (device.getLocation() == null || device.getLocation().isBlank()) {
            // Default location for COMMON devices if missing
            device.setLocation("Khu vực chung");
        }

        return deviceRepository.save(device).getId();
    }

    public Long updateDevice(Long deviceId, DeviceUpdateRequest apiRequest) {
       Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new UserMessageException("Thiết bị không tồn tại"));

       if (apiRequest.getDeviceName() != null) {
           device.setDeviceName(apiRequest.getDeviceName());
       }
       if (apiRequest.getInstallationDate() != null) {
           device.setInstallationDate(apiRequest.getInstallationDate());
       }
       if (apiRequest.getLocation() != null) {
           device.setLocation(apiRequest.getLocation());
       }
       if (apiRequest.getMaintenanceCycleDay() != null) {
           device.setMaintenanceCycleDay(apiRequest.getMaintenanceCycleDay());
       }
       if (apiRequest.getDeviceStatus() != null) {
           device.setDeviceStatus(apiRequest.getDeviceStatus());
       }

       return deviceRepository.save(device).getId();
    }

    public Void deleteDevice(Long deviceId) {
        Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new UserMessageException("Thiết bị không tồn tại"));
        deviceRepository.delete(device);
        return null;
    }
}
