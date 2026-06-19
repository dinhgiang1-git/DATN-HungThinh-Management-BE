package dinhgiang.dev.hungthinh.services.implement;

import dinhgiang.dev.hungthinh.exceptions.UserMessageException;
import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentShortGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.feedbacks.FeedbackCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.feedbacks.FeedbackGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.feedbacks.FeedbackUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.bases.Apartment;
import dinhgiang.dev.hungthinh.models.entities.bases.Feedback;
import dinhgiang.dev.hungthinh.models.entities.bases.Resident;
import dinhgiang.dev.hungthinh.models.entities.enums.FeedbackStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.FeedbackType;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import dinhgiang.dev.hungthinh.repositories.ApartmentRepository;
import dinhgiang.dev.hungthinh.repositories.DeviceRepository;
import dinhgiang.dev.hungthinh.repositories.FeedbackRepository;
import dinhgiang.dev.hungthinh.repositories.ResidentRepository;
import dinhgiang.dev.hungthinh.services.interfaces.IFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lớp cài đặt (Implementation) cho IFeedbackService.
 * Xử lý các nghiệp vụ phản hồi, kiến nghị từ cư dân.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Service
@RequiredArgsConstructor
public class FeedbackService implements IFeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final ApartmentRepository apartmentRepository;
    private final ResidentRepository residentRepository;
    private final DeviceRepository deviceRepository;
    private final AccessControlService accessControlService;

    public PageResponse<FeedbackGetResponse> getAllFeedbacks(
            int page,
            int size,
            FeedbackStatus feedbackStatus,
            FeedbackType feedbackType,
            String sortBy,
            String direction,
            String keyword,
            Long apartmentId
    ) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Long scopedApartmentId = accessControlService.resolveApartmentScopeForResident(
                apartmentId,
                "Bạn không có quyền xem phản ánh của căn hộ này"
        );

        Specification<Feedback> spec = Specification.allOf();
        if (feedbackStatus != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("feedbackStatus"), feedbackStatus));
        }
        if (feedbackType != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("feedbackType"), feedbackType));
        }
        if (scopedApartmentId != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("apartment").get("id"), scopedApartmentId));
        }
        if (keyword != null && !keyword.isBlank()) {
            String likeKeyword = "%" + keyword.toLowerCase() + "%";
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), likeKeyword),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("content")), likeKeyword),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("response")), likeKeyword)
                    ));
        }

        Page<Feedback> feedbacks = feedbackRepository.findAll(spec, pageRequest);

        List<FeedbackGetResponse> content = feedbacks.getContent().stream()
                .map(feedback -> FeedbackGetResponse.builder()
                        .feedbackId(feedback.getId())
                        .title(feedback.getTitle())
                        .content(feedback.getContent())
                        .feedbackType(feedback.getFeedbackType())
                        .feedbackStatus(feedback.getFeedbackStatus())
                        .response(feedback.getResponse())
                        .senderName(getSenderName(feedback))
                        .apartment(
                                feedback.getApartment() != null
                                    ? ApartmentShortGetResponse.builder()
                                        .id(feedback.getApartment().getId())
                                        .apartmentNumber(feedback.getApartment().getApartmentNumber())
                                        .floor(feedback.getApartment().getFloor())
                                        .block(feedback.getApartment().getBlock())
                                        .build()
                                    : null
                        )
                        .deviceId(feedback.getDevice() != null ? feedback.getDevice().getId() : null)
                        .deviceName(feedback.getDevice() != null ? feedback.getDevice().getDeviceName() : null)
                        .createdAt(feedback.getCreatedAt())
                        .build())
                .toList();

        return new PageResponse<>(
                content,
                feedbacks.getNumber() + 1,
                feedbacks.getSize(),
                feedbacks.getTotalElements(),
                feedbacks.getTotalPages()
        );
    }

    public FeedbackGetResponse getFeedbackById(Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new UserMessageException("Phản hồi không tồn tại"));
        assertResidentCanAccessFeedback(feedback);
        return FeedbackGetResponse.builder()
                .feedbackId(feedback.getId())
                .title(feedback.getTitle())
                .content(feedback.getContent())
                .feedbackType(feedback.getFeedbackType())
                .feedbackStatus(feedback.getFeedbackStatus())
                .response(feedback.getResponse())
                .senderName(getSenderName(feedback))
                .apartment(
                        feedback.getApartment() != null
                            ? ApartmentShortGetResponse.builder()
                                .id(feedback.getApartment().getId())
                                .apartmentNumber(feedback.getApartment().getApartmentNumber())
                                .floor(feedback.getApartment().getFloor())
                                .block(feedback.getApartment().getBlock())
                                .build()
                            : null
                )
                .deviceId(feedback.getDevice() != null ? feedback.getDevice().getId() : null)
                .deviceName(feedback.getDevice() != null ? feedback.getDevice().getDeviceName() : null)
                .createdAt(feedback.getCreatedAt())
                .build();
    }

    private String getSenderName(Feedback feedback) {
        if (feedback.getSender() != null) return feedback.getSender().getFullName();
        if (feedback.getApartment() != null && feedback.getApartment().getResidents() != null) {
            return feedback.getApartment().getResidents().stream()
                    .filter(r -> dinhgiang.dev.hungthinh.models.entities.enums.RelationshipType.OWNER.equals(r.getRelationship()))
                    .map(dinhgiang.dev.hungthinh.models.entities.bases.Resident::getFullName)
                    .findFirst()
                    .orElse("Cư dân");
        }
        return "Cư dân";
    }

    public Long updateFeedback(Long feedbackId, FeedbackUpdateRequest apiRequest) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new UserMessageException("Phản hồi không tồn tại"));
        assertResidentCanAccessFeedback(feedback);
        if (apiRequest.getTitle() != null) {
            feedback.setTitle(apiRequest.getTitle());
        }
        if (apiRequest.getContent() != null) {
            feedback.setContent(apiRequest.getContent());
        }
        if (apiRequest.getFeedbackStatus() != null) {
            feedback.setFeedbackStatus(apiRequest.getFeedbackStatus());
        }
        if (apiRequest.getApartmentId() != null) {
            Apartment apartment = apartmentRepository.findById(apiRequest.getApartmentId())
                    .orElseThrow(() -> new UserMessageException("Căn hộ không tồn tại"));
            accessControlService.assertResidentCanAccessApartment(
                    apartment,
                    "Bạn không có quyền chuyển phản ánh sang căn hộ này"
            );
            feedback.setApartment(apartment);
        }
        if (apiRequest.getResponse() != null) {
            feedback.setResponse(apiRequest.getResponse());
        }
        feedbackRepository.save(feedback);
        return feedback.getId();
    }

    public Long createFeedback(FeedbackCreateRequest apiRequest) {
        Apartment apartment = apartmentRepository.findById(apiRequest.getApartmentId())
                .orElseThrow(() -> new UserMessageException("Căn hộ không tồn tại"));
        accessControlService.assertResidentCanAccessApartment(
                apartment,
                "Bạn không có quyền tạo phản ánh cho căn hộ này"
        );

        Feedback feedback = new Feedback();
        feedback.setTitle(apiRequest.getTitle());
        feedback.setContent(apiRequest.getContent());
        feedback.setFeedbackType(apiRequest.getFeedbackType() != null ? apiRequest.getFeedbackType() : FeedbackType.GENERAL);
        feedback.setFeedbackStatus(apiRequest.getFeedbackStatus() != null ? apiRequest.getFeedbackStatus() : FeedbackStatus.PENDING);
        feedback.setApartment(apartment);
        if (accessControlService.isResident()) {
            feedback.setSender(accessControlService.currentResident());
        } else if (apiRequest.getSenderId() != null) {
            Resident sender = residentRepository.findById(apiRequest.getSenderId())
                    .orElseThrow(() -> new UserMessageException("Cư dân không tồn tại"));
            feedback.setSender(sender);
        }
        if (apiRequest.getDeviceId() != null) {
            var device = deviceRepository.findById(apiRequest.getDeviceId())
                    .orElseThrow(() -> new UserMessageException("Thiết bị không tồn tại"));
            accessControlService.assertResidentCanAccessApartment(
                    device.getApartment(),
                    "Bạn không có quyền tạo phản ánh cho thiết bị này"
            );
            feedback.setDevice(device);
        }
        feedbackRepository.save(feedback);

        return feedback.getId();
    }

    public Void deleteFeedback(Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new UserMessageException("Phản hồi không tồn tại"));
        assertResidentCanAccessFeedback(feedback);
        feedbackRepository.delete(feedback);
        return null;
    }

    private void assertResidentCanAccessFeedback(Feedback feedback) {
        accessControlService.assertResidentCanAccessApartment(
                feedback.getApartment(),
                "Bạn không có quyền truy cập phản ánh này"
        );
    }
}
