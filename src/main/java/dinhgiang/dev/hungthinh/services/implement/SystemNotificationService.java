package dinhgiang.dev.hungthinh.services.implement;

import dinhgiang.dev.hungthinh.components.NotificationWebSocketHandler;
import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentShortGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.systemnotifications.SystemNotificationGetResponse;
import dinhgiang.dev.hungthinh.models.entities.bases.*;
import dinhgiang.dev.hungthinh.models.entities.enums.RelationshipType;
import dinhgiang.dev.hungthinh.models.entities.global.PageResponse;
import dinhgiang.dev.hungthinh.repositories.ApartmentRepository;
import dinhgiang.dev.hungthinh.repositories.NotificationReceiverRepository;
import dinhgiang.dev.hungthinh.repositories.NotificationRepository;
import dinhgiang.dev.hungthinh.repositories.SystemNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
/**
 * Lớp dịch vụ quản lý Thông báo hệ thống tự động (System Notification).
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Service
@RequiredArgsConstructor
public class SystemNotificationService {

    private final SystemNotificationRepository systemNotificationRepository;
    private final ApartmentRepository apartmentRepository;
    private final NotificationWebSocketHandler notificationWebSocketHandler;
    private final NotificationReceiverRepository notificationReceiverRepository;
    private final NotificationRepository notificationRepository;

    /**
     * Tạo thông báo hệ thống cho một căn hộ
     */
    @Transactional
    public void notifyApartment(String title, String content, Long apartmentId) {
        try {
            Apartment apartment = apartmentRepository.findById(apartmentId).orElse(null);
            if (apartment == null) return;

            List<Resident> residents = apartment.getResidents();
            if (residents == null || residents.isEmpty()) return;

            List<Resident> owners = residents.stream()
                    .filter(r -> r.getRelationship() == RelationshipType.OWNER)
                    .toList();
            if (owners.isEmpty()) return;

            for (Resident owner : owners) {
                SystemNotification sysNotif = SystemNotification.builder()
                        .title(title)
                        .content(content)
                        .sendTime(LocalDateTime.now())
                        .isRead(false)
                        .resident(owner)
                        .apartment(apartment)
                        .build();
                systemNotificationRepository.save(sysNotif);

                // Push realtime qua WebSocket
                notificationWebSocketHandler.sendToResident(owner.getId(),
                        SystemNotificationGetResponse.builder()
                                .id(sysNotif.getId())
                                .title(sysNotif.getTitle())
                                .content(sysNotif.getContent())
                                .sendTime(sysNotif.getSendTime())
                                .isRead(false)
                                .build());
            }

            log.info("System notification sent to apartment {}: {}", apartmentId, title);
        } catch (Exception e) {
            log.error("Failed to send system notification: {}", e.getMessage());
        }
    }

    /**
     * Lấy danh sách thông báo hệ thống của resident
     */
    public PageResponse<SystemNotificationGetResponse> getByResident(Long residentId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("sendTime").descending());
        Page<SystemNotification> result = systemNotificationRepository.findByResidentId(residentId, pageRequest);

        List<SystemNotificationGetResponse> content = result.getContent().stream()
                .map(sn -> {
                    // Lấy tên người gửi từ Notification gốc (nếu có)
                    String senderName = null;
                    if (sn.getNotificationId() != null) {
                        try {
                            Notification notif = notificationRepository.findById(sn.getNotificationId()).orElse(null);
                            if (notif != null && notif.getSender() != null) {
                                senderName = notif.getSender().getFullName();
                            }
                        } catch (Exception e) {
                            log.warn("Could not fetch sender for notification {}: {}", sn.getNotificationId(), e.getMessage());
                        }
                    }

                    return SystemNotificationGetResponse.builder()
                            .id(sn.getId())
                            .title(sn.getTitle())
                            .content(sn.getContent())
                            .sendTime(sn.getSendTime())
                            .isRead(sn.getIsRead())
                            .readAt(sn.getReadAt())
                            .senderName(senderName)
                            .apartment(sn.getApartment() != null
                                    ? ApartmentShortGetResponse.builder()
                                        .apartmentNumber(sn.getApartment().getApartmentNumber())
                                        .floor(sn.getApartment().getFloor())
                                        .block(sn.getApartment().getBlock())
                                        .build()
                                    : null)
                            .build();
                })
                .toList();

        return new PageResponse<>(
                content,
                result.getNumber() + 1,
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    /**
     * Đánh dấu đã đọc + đồng bộ trạng thái đọc sang NotificationReceiver
     */
    @Transactional
    public void markAsRead(Long systemNotificationId) {
        SystemNotification sn = systemNotificationRepository.findById(systemNotificationId).orElse(null);
        if (sn != null && !Boolean.TRUE.equals(sn.getIsRead())) {
            sn.setIsRead(true);
            sn.setReadAt(LocalDateTime.now());
            systemNotificationRepository.save(sn);

            // Đồng bộ trạng thái đọc sang NotificationReceiver (cho Admin FE)
            if (sn.getNotificationId() != null && sn.getResident() != null) {
                notificationReceiverRepository
                        .findByNotificationIdAndResidentId(sn.getNotificationId(), sn.getResident().getId())
                        .ifPresent(nr -> {
                            if (!Boolean.TRUE.equals(nr.getIsRead())) {
                                nr.setIsRead(true);
                                nr.setReadAt(LocalDateTime.now());
                                notificationReceiverRepository.save(nr);
                            }
                        });
            }
        }
    }

    /**
     * Đánh dấu toàn bộ thông báo của resident là đã đọc.
     */
    @Transactional
    public void markAllAsRead(Long residentId) {
        LocalDateTime now = LocalDateTime.now();

        List<SystemNotification> unreadSystemNotifications =
                systemNotificationRepository.findByResidentIdAndIsReadFalse(residentId);
        unreadSystemNotifications.forEach(sn -> {
            sn.setIsRead(true);
            sn.setReadAt(now);
        });
        systemNotificationRepository.saveAll(unreadSystemNotifications);

        List<NotificationReceiver> unreadReceivers =
                notificationReceiverRepository.findByResidentIdAndIsReadFalse(residentId);
        unreadReceivers.forEach(receiver -> {
            receiver.setIsRead(true);
            receiver.setReadAt(now);
        });
        notificationReceiverRepository.saveAll(unreadReceivers);
    }

    /**
     * Đếm số thông báo chưa đọc
     */
    public long countUnread(Long residentId) {
        return systemNotificationRepository.countByResidentIdAndIsReadFalse(residentId);
    }
}
