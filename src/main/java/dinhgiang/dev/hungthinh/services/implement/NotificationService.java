package dinhgiang.dev.hungthinh.services.implement;

import dinhgiang.dev.hungthinh.components.NotificationWebSocketHandler;
import dinhgiang.dev.hungthinh.exceptions.UserMessageException;
import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentShortGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.notifications.NotificationCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.notifications.NotificationGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.notifications.NotificationUpdateRequest;
import dinhgiang.dev.hungthinh.models.dtos.notifications.ReceiverGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.residents.ResidentShortGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.users.UserShortGetResponse;
import dinhgiang.dev.hungthinh.models.entities.bases.*;
import dinhgiang.dev.hungthinh.models.entities.enums.RelationshipType;
import dinhgiang.dev.hungthinh.models.entities.enums.TargetType;
import dinhgiang.dev.hungthinh.repositories.SystemNotificationRepository;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import dinhgiang.dev.hungthinh.repositories.ApartmentRepository;
import dinhgiang.dev.hungthinh.repositories.NotificationReceiverRepository;
import dinhgiang.dev.hungthinh.repositories.NotificationRepository;
import dinhgiang.dev.hungthinh.repositories.ResidentRepository;
import dinhgiang.dev.hungthinh.repositories.UserRepository;
import dinhgiang.dev.hungthinh.services.interfaces.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
/*
  Lớp cài đặt (Implementation) cho INotificationService.
  Xử lý nghiệp vụ thông báo: tạo thông báo, gửi đến các đối tượng (block, căn hộ, tất cả) và theo dõi trạng thái đọc.

  @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ResidentRepository residentRepository;
    private final ApartmentRepository apartmentRepository;
    private final NotificationReceiverRepository notificationReceiverRepository;
    private final NotificationWebSocketHandler notificationWebSocketHandler;
    private final SystemNotificationRepository systemNotificationRepository;

    public PageResponse<NotificationGetResponse> getAllNotifications(
            int page,
            int size,
            TargetType targetType,
            String sortBy,
            String direction,
            String keyword
    ) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Specification<Notification> spec = Specification.allOf();
        if (targetType != null) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("targetType"), targetType));
        }
        if (keyword !=null && !keyword.isBlank()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.or(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), keyword),
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("content")), keyword)
                    ));
        }

        Page<Notification> notifications = notificationRepository.findAll(spec, pageRequest);

        List<NotificationGetResponse> content = notifications.getContent().stream()
                .map(notification -> {
                    long total = notificationReceiverRepository.countByNotificationId(notification.getId());
                    long read = notificationReceiverRepository.countByNotificationIdAndIsReadTrue(notification.getId());
                    return NotificationGetResponse.builder()
                        .notificationId(notification.getId())
                        .title(notification.getTitle())
                        .content(notification.getContent())
                        .sendTime(notification.getSendTime())
                        .targetType(notification.getTargetType())
                        .block(notification.getBlock())
                        .apartmentId(notification.getApartmentId())
                        .sender(
                                notification.getSender() != null
                                ? UserShortGetResponse.builder()
                                        .userId(notification.getSender().getId())
                                        .fullName(notification.getSender().getFullName())
                                        .phoneNumber(notification.getSender().getPhoneNumber())
                                        .build()
                                : null)
                        .readCount(read)
                        .totalReceivers(total)
                        .build();
                })
                .toList();
        return new PageResponse<>(
                content,
                notifications.getNumber() + 1,
                notifications.getSize(),
                notifications.getTotalElements(),
                notifications.getTotalPages()
        );
    }

    public PageResponse<NotificationGetResponse> getNotificationsByResident(
            Long residentId,
            int page,
            int size,
            String sortBy,
            String direction,
            String keyword
    ) {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);

        // Filter: only notifications where this resident is a receiver
        Specification<Notification> spec = (root, query, cb) -> {
            var receivers = root.join("receivers");
            return cb.equal(receivers.get("resident").get("id"), residentId);
        };

        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.or(
                            cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"),
                            cb.like(cb.lower(root.get("content")), "%" + keyword.toLowerCase() + "%")
                    ));
        }

        Page<Notification> notifications = notificationRepository.findAll(spec, pageRequest);

        List<NotificationGetResponse> content = notifications.getContent().stream()
                .map(notification -> NotificationGetResponse.builder()
                        .notificationId(notification.getId())
                        .title(notification.getTitle())
                        .content(notification.getContent())
                        .sendTime(notification.getSendTime())
                        .targetType(notification.getTargetType())
                        .block(notification.getBlock())
                        .apartmentId(notification.getApartmentId())
                        .sender(
                                notification.getSender() != null
                                ? UserShortGetResponse.builder()
                                        .userId(notification.getSender().getId())
                                        .fullName(notification.getSender().getFullName())
                                        .phoneNumber(notification.getSender().getPhoneNumber())
                                        .build()
                                : null)
                        .build())
                .toList();

        return new PageResponse<>(
                content,
                notifications.getNumber() + 1,
                notifications.getSize(),
                notifications.getTotalElements(),
                notifications.getTotalPages()
        );
    }

    public NotificationGetResponse getNotificationById(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new UserMessageException("Thông báo không tồn tại"));

        // Lookup apartment details if apartmentId exists
        ApartmentShortGetResponse apartmentResponse = null;
        if (notification.getApartmentId() != null) {
            var aptOpt = apartmentRepository.findById(notification.getApartmentId());
            if (aptOpt.isPresent()) {
                var apt = aptOpt.get();
                apartmentResponse = ApartmentShortGetResponse.builder()
                        .apartmentNumber(apt.getApartmentNumber())
                        .floor(apt.getFloor())
                        .block(apt.getBlock())
                        .build();
            }
        }

        return NotificationGetResponse.builder()
                .notificationId(notification.getId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .sendTime(notification.getSendTime())
                .targetType(notification.getTargetType())
                .block(notification.getBlock())
                .apartmentId(notification.getApartmentId())
                .apartment(apartmentResponse)
                .sender(
                        notification.getSender() != null
                            ? UserShortGetResponse.builder()
                                .userId(notification.getSender().getId())
                                .fullName(notification.getSender().getFullName())
                                .phoneNumber(notification.getSender().getPhoneNumber())
                                .build()
                            : null)
                .receivers(
                        notification.getReceivers() != null
                            ? notification.getReceivers().stream()
                                .map(notificationReceiver -> ReceiverGetResponse.builder()
                                        .receiverNotificationId(notificationReceiver.getId())
                                        .resident(
                                                notificationReceiver.getResident() != null
                                                    ? ResidentShortGetResponse.builder()
                                                        .residentId(notificationReceiver.getResident().getId())
                                                        .fullName(notificationReceiver.getResident().getFullName())
                                                        .email(notificationReceiver.getResident().getEmail())
                                                        .build()
                                                    : null)
                                        .isRead(notificationReceiver.getIsRead())
                                        .readAt(notificationReceiver.getReadAt())
                                        .build())
                                .toList()
                            : List.of()
                ).build();
    }

    @Transactional
    public Long createNotification(NotificationCreateRequest apiRequest) {

        //Tạo thông báo
        Notification notification = new Notification();
        notification.setTitle(apiRequest.getTitle());
        notification.setContent(apiRequest.getContent());
        notification.setTargetType(apiRequest.getTargetType());
        notification.setSendTime(LocalDateTime.now());
        // Lưu block và apartmentId để edit modal có thể fill lại
        if (apiRequest.getBlock() != null) {
            notification.setBlock(apiRequest.getBlock());
        }
        if (apiRequest.getApartmentId() != null) {
            notification.setApartmentId(apiRequest.getApartmentId());
        }
        if (apiRequest.getSenderId() != null) {
            User sender = userRepository.findById(apiRequest.getSenderId())
                    .orElseThrow(()-> new UserMessageException("Người gửi không tồn tại"));
            notification.setSender(sender);
        }

        //Lấy danh sách theo target
        List<Resident> residents;
        List<Apartment> apartments;
        switch (apiRequest.getTargetType()) {
            case ALL -> residents = residentRepository.findByRelationship(RelationshipType.OWNER);
            case BLOCK -> {
                if (apiRequest.getBlock() == null || apiRequest.getBlock().isBlank()) {
                    throw new UserMessageException("Thiếu block");
                }
                apartments = apartmentRepository.findByBlock(apiRequest.getBlock());
                residents = apartments.stream()
                        .flatMap(apartment -> apartment.getResidents().stream())
                        .filter(r -> r.getRelationship() == RelationshipType.OWNER)
                        .distinct()
                        .toList();
            }
            case APARTMENT -> {
                if (apiRequest.getApartmentId() == null) {
                    throw new UserMessageException("Thiếu số nhà");
                }
                Apartment apartment = apartmentRepository.findById(apiRequest.getApartmentId())
                        .orElseThrow(() -> new UserMessageException("Căn hộ không tồn tại"));
                residents = apartment.getResidents().stream()
                        .filter(r -> r.getRelationship() == RelationshipType.OWNER)
                        .toList();
            }
            default -> throw new UserMessageException("TargetType không hợp lệ");
        }

        //Tạo NotificationReceiver
        List<NotificationReceiver> receivers = residents.stream()
                .map(resident -> {
                    NotificationReceiver nr = new NotificationReceiver();
                    nr.setNotification(notification);
                    nr.setResident(resident);
                    nr.setIsRead(false);
                    return nr;
                }).toList();

        notification.setReceivers(receivers);
        notificationRepository.save(notification);

        // Tạo SystemNotification cho mỗi resident (danh sách thông báo chung)
        for (Resident resident : residents) {
            SystemNotification sysNotif = SystemNotification.builder()
                    .title(notification.getTitle())
                    .content(notification.getContent())
                    .sendTime(notification.getSendTime())
                    .isRead(false)
                    .resident(resident)
                    .notificationId(notification.getId())
                    .build();
            systemNotificationRepository.save(sysNotif);
        }

        for (NotificationReceiver receiver : receivers) {
            Long residentId = receiver.getResident().getId();
            notificationWebSocketHandler.sendToResident(residentId,
                    NotificationGetResponse.builder()
                            .notificationId(notification.getId())
                            .title(notification.getTitle())
                            .content(notification.getContent())
                            .sendTime(notification.getSendTime())
                            .build());
        }
        return notification.getId();
    }

    public Long updateNotification(Long notificationId, NotificationUpdateRequest apiRequest) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new UserMessageException("Thông báo không tồn tại"));

        if (apiRequest.getTitle() != null && !apiRequest.getTitle().isBlank()) {
            notification.setTitle(apiRequest.getTitle());
        }
        if(apiRequest.getContent() != null && !apiRequest.getContent().isBlank()) {
            notification.setContent(apiRequest.getContent());
        }
        if (apiRequest.getTargetType() != null) {
            notification.setTargetType(apiRequest.getTargetType());
        }

        notificationRepository.save(notification);
        return notification.getId();
    }

    public Void deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(()-> new UserMessageException("Thông báo không tồn tại"));
        notificationRepository.delete(notification);
        return null;
    }

    public PageResponse<ReceiverGetResponse> getReceiversByNotification(Long notificationId, int page, int size, String keyword, Boolean isRead) {
        // Verify notification exists
        notificationRepository.findById(notificationId)
                .orElseThrow(() -> new UserMessageException("Thông báo không tồn tại"));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").ascending());

        Specification<NotificationReceiver> spec = (root, query, cb) ->
                cb.equal(root.get("notification").get("id"), notificationId);

        if (isRead != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("isRead"), isRead));
        }

        if (keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> {
                var resident = root.join("resident");
                return cb.or(
                        cb.like(cb.lower(resident.get("fullName")), kw),
                        cb.like(cb.lower(resident.get("email")), kw)
                );
            });
        }

        Page<NotificationReceiver> receivers = notificationReceiverRepository.findAll(spec, pageRequest);

        List<ReceiverGetResponse> content = receivers.getContent().stream()
                .map(nr -> ReceiverGetResponse.builder()
                        .receiverNotificationId(nr.getId())
                        .resident(
                                nr.getResident() != null
                                    ? ResidentShortGetResponse.builder()
                                        .residentId(nr.getResident().getId())
                                        .fullName(nr.getResident().getFullName())
                                        .email(nr.getResident().getEmail())
                                        .build()
                                    : null
                        )
                        .isRead(nr.getIsRead())
                        .readAt(nr.getReadAt())
                        .build())
                .toList();

        return new PageResponse<>(
                content,
                receivers.getNumber() + 1,
                receivers.getSize(),
                receivers.getTotalElements(),
                receivers.getTotalPages()
        );
    }

    @Transactional
    public void markAsRead(Long notificationId, Long residentId) {
        NotificationReceiver receiver = notificationReceiverRepository
                .findByNotificationIdAndResidentId(notificationId, residentId)
                .orElse(null);
        if (receiver != null && !Boolean.TRUE.equals(receiver.getIsRead())) {
            receiver.setIsRead(true);
            receiver.setReadAt(LocalDateTime.now());
            notificationReceiverRepository.save(receiver);
        }
    }
}
