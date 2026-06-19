package dinhgiang.dev.hungthinh.repositories;

import dinhgiang.dev.hungthinh.models.entities.bases.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repository qu?n lý truy xu?t d? li?u cho Notification.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {
}
