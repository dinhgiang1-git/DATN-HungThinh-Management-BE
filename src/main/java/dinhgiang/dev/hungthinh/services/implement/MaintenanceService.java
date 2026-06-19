package dinhgiang.dev.hungthinh.services.implement;

import dinhgiang.dev.hungthinh.exceptions.UserMessageException;
import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentShortGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.devices.DeviceShortGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.maintenances.MaintenanceCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.maintenances.MaintenanceGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.maintenances.MaintenanceUpdateRequest;
import dinhgiang.dev.hungthinh.models.dtos.users.TechnicianShortGetResponse;
import dinhgiang.dev.hungthinh.models.entities.bases.Device;
import dinhgiang.dev.hungthinh.models.entities.bases.Maintenance;
import dinhgiang.dev.hungthinh.models.entities.bases.User;
import dinhgiang.dev.hungthinh.models.entities.enums.MaintenanceStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.UserRole;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import dinhgiang.dev.hungthinh.repositories.FeedbackRepository;
import dinhgiang.dev.hungthinh.repositories.ApartmentRepository;
import dinhgiang.dev.hungthinh.models.entities.enums.FeedbackStatus;
import dinhgiang.dev.hungthinh.repositories.DeviceRepository;
import dinhgiang.dev.hungthinh.repositories.MaintenanceRepository;
import dinhgiang.dev.hungthinh.repositories.UserRepository;
import dinhgiang.dev.hungthinh.services.interfaces.IMaintenanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Lớp cài đặt (Implementation) cho IMaintenanceService.
 * Xử lý nghiệp vụ bảo trì: phân công kỹ thuật viên, cập nhật trạng thái bảo trì thiết bị.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Service
@RequiredArgsConstructor
public class MaintenanceService implements IMaintenanceService {

    private final MaintenanceRepository maintenanceRepository;
    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final FeedbackRepository feedbackRepository;
    private final ApartmentRepository apartmentRepository;

    public PageResponse<MaintenanceGetResponse> getAllMaintenance(
            int page,
            int size,
            MaintenanceStatus maintenanceStatus,
            String sortBy,
            String direction,
            String keyword
    ) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Specification<Maintenance> spec = Specification.allOf();
        if (maintenanceStatus != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("maintenanceStatus"), maintenanceStatus));
        }
        if (keyword != null && !keyword.isBlank()) {
            String likeKeyword = "%" + keyword + "%";
            Specification<Maintenance> keywordSpec = (root, query, cb) ->
                    cb.like(cb.lower(root.get("description")), likeKeyword);
            try {
                BigDecimal cost = new BigDecimal(keyword);
                keywordSpec = keywordSpec.or((root, query, cb) ->
                        cb.equal(root.get("cost"), cost));
            }catch (NumberFormatException ignore) {}

            spec = spec.and(keywordSpec);
        }

        Page<Maintenance> maintenances = maintenanceRepository.findAll(spec, pageRequest);

        List<MaintenanceGetResponse> content = maintenances.getContent().stream()
                .map(maintenance -> MaintenanceGetResponse.builder()
                        .maintenanceId(maintenance.getId())
                        .completedDate(maintenance.getCompletedDate())
                        .startedDate(maintenance.getStartedDate())
                        .cost(maintenance.getCost())
                        .description(maintenance.getDescription())
                        .maintenanceStatus(maintenance.getMaintenanceStatus())
                        .device(
                                maintenance.getDevice() != null
                                        ? DeviceShortGetResponse.builder()
                                        .deviceId(maintenance.getDevice().getId())
                                        .deviceName(maintenance.getDevice().getDeviceName())
                                        .build()
                                        : null
                        )
                        .apartment(
                                maintenance.getApartment() != null
                                        ? ApartmentShortGetResponse.builder()
                                        .id(maintenance.getApartment().getId())
                                        .apartmentNumber(maintenance.getApartment().getApartmentNumber())
                                        .block(maintenance.getApartment().getBlock())
                                        .floor(maintenance.getApartment().getFloor())
                                        .build()
                                        : null
                        )
                        .technician(
                                maintenance.getTechnicians() != null
                                        ? maintenance.getTechnicians().stream()
                                        .map(user -> TechnicianShortGetResponse.builder()
                                                .technicianId(user.getId())
                                                .technicianName(user.getFullName())
                                                .build()
                                        )
                                        .toList()
                                        : List.of()
                        )
                        .feedback(
                                maintenance.getFeedback() != null
                                        ? dinhgiang.dev.hungthinh.models.dtos.feedbacks.FeedbackShortGetResponse.builder()
                                        .feedbackId(maintenance.getFeedback().getId())
                                        .title(maintenance.getFeedback().getTitle())
                                        .content(maintenance.getFeedback().getContent())
                                        .senderName(
                                                maintenance.getFeedback().getSender() != null
                                                        ? maintenance.getFeedback().getSender().getFullName()
                                                        : (maintenance.getFeedback().getApartment() != null && maintenance.getFeedback().getApartment().getResidents() != null
                                                                ? maintenance.getFeedback().getApartment().getResidents().stream()
                                                                .filter(r -> "OWNER".equals(r.getRelationship().name()))
                                                                .map(dinhgiang.dev.hungthinh.models.entities.bases.Resident::getFullName)
                                                                .findFirst().orElse("Cư dân")
                                                                : "Cư dân")
                                        )
                                        .phoneNumber(
                                                maintenance.getFeedback().getSender() != null
                                                        ? maintenance.getFeedback().getSender().getPhoneNumber()
                                                        : (maintenance.getFeedback().getApartment() != null && maintenance.getFeedback().getApartment().getResidents() != null
                                                                ? maintenance.getFeedback().getApartment().getResidents().stream()
                                                                .filter(r -> "OWNER".equals(r.getRelationship().name()))
                                                                .map(dinhgiang.dev.hungthinh.models.entities.bases.Resident::getPhoneNumber)
                                                                .findFirst().orElse("Chưa có")
                                                                : "Chưa có")
                                        )
                                        .apartmentName(maintenance.getFeedback().getApartment() != null ? maintenance.getFeedback().getApartment().getApartmentNumber() : "N/A")
                                        .build()
                                        : null
                        )
                        .build()
                ).toList();

        return new PageResponse<>(
                content,
                maintenances.getNumber() + 1,
                maintenances.getSize(),
                maintenances.getTotalElements(),
                maintenances.getTotalPages()
        );
    }

    public MaintenanceGetResponse getMaintenanceById(Long maintenanceId) {
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId).orElseThrow(() -> new UserMessageException("Bảo trì không tồn tại"));

        return MaintenanceGetResponse.builder()
                .maintenanceId(maintenance.getId())
                .completedDate(maintenance.getCompletedDate())
                .startedDate(maintenance.getStartedDate())
                .cost(maintenance.getCost())
                .description(maintenance.getDescription())
                .maintenanceStatus(maintenance.getMaintenanceStatus())
                .device(
                        maintenance.getDevice() != null
                                ? DeviceShortGetResponse.builder()
                                .deviceId(maintenance.getDevice().getId())
                                .deviceName(maintenance.getDevice().getDeviceName())
                                .build()
                                : null
                )
                .apartment(
                        maintenance.getApartment() != null
                                ? ApartmentShortGetResponse.builder()
                                .id(maintenance.getApartment().getId())
                                .apartmentNumber(maintenance.getApartment().getApartmentNumber())
                                .block(maintenance.getApartment().getBlock())
                                .floor(maintenance.getApartment().getFloor())
                                .build()
                                : null
                )
                .technician(
                        maintenance.getTechnicians() != null
                                ? maintenance.getTechnicians().stream()
                                .map(user -> TechnicianShortGetResponse.builder()
                                        .technicianId(user.getId())
                                        .technicianName(user.getFullName())
                                        .build()
                                )
                                .toList()
                                : List.of()
                )
                .feedback(
                        maintenance.getFeedback() != null
                                ? dinhgiang.dev.hungthinh.models.dtos.feedbacks.FeedbackShortGetResponse.builder()
                                .feedbackId(maintenance.getFeedback().getId())
                                .title(maintenance.getFeedback().getTitle())
                                .content(maintenance.getFeedback().getContent())
                                .senderName(
                                        maintenance.getFeedback().getSender() != null
                                                ? maintenance.getFeedback().getSender().getFullName()
                                                : (maintenance.getFeedback().getApartment() != null && maintenance.getFeedback().getApartment().getResidents() != null
                                                        ? maintenance.getFeedback().getApartment().getResidents().stream()
                                                        .filter(r -> "OWNER".equals(r.getRelationship().name()))
                                                        .map(dinhgiang.dev.hungthinh.models.entities.bases.Resident::getFullName)
                                                        .findFirst().orElse("Cư dân")
                                                        : "Cư dân")
                                )
                                .phoneNumber(
                                        maintenance.getFeedback().getSender() != null
                                                ? maintenance.getFeedback().getSender().getPhoneNumber()
                                                : (maintenance.getFeedback().getApartment() != null && maintenance.getFeedback().getApartment().getResidents() != null
                                                        ? maintenance.getFeedback().getApartment().getResidents().stream()
                                                        .filter(r -> "OWNER".equals(r.getRelationship().name()))
                                                        .map(dinhgiang.dev.hungthinh.models.entities.bases.Resident::getPhoneNumber)
                                                        .findFirst().orElse("Chưa có")
                                                        : "Chưa có")
                                )
                                .apartmentName(maintenance.getFeedback().getApartment() != null ? maintenance.getFeedback().getApartment().getApartmentNumber() : "N/A")
                                .build()
                                : null
                )
                .build();
    }

    @Transactional
    public Long updateMaintenance(Long maintenanceId, MaintenanceUpdateRequest apiReqeust) {
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new UserMessageException("Bảo trì không tồn tại"));

        if (apiReqeust.getDeviceId() != null) {
           Device device = deviceRepository.findById(apiReqeust.getDeviceId())
                   .orElseThrow(() -> new UserMessageException("Thiết bị không tồn tại"));
           maintenance.setDevice(device);
        }
        if (apiReqeust.getCompletedDate() != null) {
            maintenance.setCompletedDate(apiReqeust.getCompletedDate());
        }
        if (apiReqeust.getStartedDate() != null) {
            maintenance.setStartedDate(apiReqeust.getStartedDate());
        }
        if (apiReqeust.getCost() != null) {
            maintenance.setCost(apiReqeust.getCost());
        }
        if (apiReqeust.getDescription() != null) {
            maintenance.setDescription(apiReqeust.getDescription());
        }
        if (apiReqeust.getMaintenanceStatus() != null) {
            maintenance.setMaintenanceStatus(apiReqeust.getMaintenanceStatus());
            // Tự động đóng phản ánh nếu bảo trì hoàn tất
            if (apiReqeust.getMaintenanceStatus() == MaintenanceStatus.COMPLETED && maintenance.getFeedback() != null) {
                maintenance.getFeedback().setFeedbackStatus(FeedbackStatus.RESOLVED);
                feedbackRepository.save(maintenance.getFeedback());
            }
        }
        if (apiReqeust.getTechnicianId() != null) {
            // Clear existing technicians
            maintenance.setTechnicians(new ArrayList<>());
            // Add new technicians
            List<User> newUsers = userRepository.findAllById(apiReqeust.getTechnicianId());
            for (User user : newUsers) {
                if (user.getUserRole() != UserRole.TECHNICIAN) {
                    throw new UserMessageException("User " + user.getFullName() +" không phải là kỹ thuật viên");
                }
            }
            maintenance.setTechnicians(newUsers);
        }
        maintenanceRepository.save(maintenance);
        return maintenance.getId();
    }

    @Transactional
    public Long createMaintenance(MaintenanceCreateRequest apiReqeust) {
        Maintenance maintenance = new Maintenance();
        maintenance.setCompletedDate(apiReqeust.getCompletedDate());
        maintenance.setStartedDate(apiReqeust.getStartedDate());
        maintenance.setCost(apiReqeust.getCost());
        maintenance.setDescription(apiReqeust.getDescription());
        maintenance.setMaintenanceStatus(apiReqeust.getMaintenanceStatus());

        if (apiReqeust.getDeviceId() != null) {
            Device device = deviceRepository.findById(apiReqeust.getDeviceId())
                    .orElseThrow(() -> new UserMessageException("Thiết bị không tồn tại"));
            maintenance.setDevice(device);
        }

        if (apiReqeust.getTechnicianId() != null && !apiReqeust.getTechnicianId().isEmpty()) {
            List<User> users = userRepository.findAllById(apiReqeust.getTechnicianId());
            if (users.size() != apiReqeust.getTechnicianId().size()) {
                throw new UserMessageException("Có technician không tồn tại");
            }
            for (User user : users) {
                if (user.getUserRole() != UserRole.TECHNICIAN) {
                    throw new UserMessageException("User " + user.getFullName() +" không phải là kỹ thuật viên");
                }
            }
            maintenance.setTechnicians(users);
        }

        if (apiReqeust.getFeedbackId() != null) {
            feedbackRepository.findById(apiReqeust.getFeedbackId()).ifPresent(f -> {
                maintenance.setFeedback(f);
                // Nếu bảo trì tạo xong đã hoàn thành luôn thì giải quyết feedback
                if (apiReqeust.getMaintenanceStatus() == MaintenanceStatus.COMPLETED) {
                    f.setFeedbackStatus(FeedbackStatus.RESOLVED);
                } else {
                    f.setFeedbackStatus(FeedbackStatus.IN_PROGRESS);
                }
                feedbackRepository.save(f);
                // Tự động gán căn hộ từ phản ánh nếu bảo trì không chọn thiết bị chung
                if (f.getApartment() != null && maintenance.getApartment() == null) {
                    maintenance.setApartment(f.getApartment());
                }
            });
        }

        if (apiReqeust.getApartmentId() != null) {
            apartmentRepository.findById(apiReqeust.getApartmentId()).ifPresent(maintenance::setApartment);
        }

        maintenanceRepository.save(maintenance);
        return maintenance.getId();
    }

    public Void deleteMaintenance(Long maintenanceId) {
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
                .orElseThrow(() -> new UserMessageException("Bảo trì không tồn tại"));
        maintenanceRepository.delete(maintenance);
        return null;
    }
}
