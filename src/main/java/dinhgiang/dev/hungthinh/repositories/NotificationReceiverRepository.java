package dinhgiang.dev.hungthinh.repositories;

import dinhgiang.dev.hungthinh.models.entities.bases.NotificationReceiver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * Repository qu?n lý truy xu?t d? li?u cho NotificationReceiver.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface NotificationReceiverRepository extends JpaRepository<NotificationReceiver, Long>, JpaSpecificationExecutor<NotificationReceiver> {
    Optional<NotificationReceiver> findByNotificationIdAndResidentId(Long notificationId, Long residentId);

    List<NotificationReceiver> findByResidentIdAndIsReadFalse(Long residentId);

    Page<NotificationReceiver> findByNotificationId(Long notificationId, Pageable pageable);

    long countByNotificationIdAndIsReadTrue(Long notificationId);

    long countByNotificationId(Long notificationId);
}
