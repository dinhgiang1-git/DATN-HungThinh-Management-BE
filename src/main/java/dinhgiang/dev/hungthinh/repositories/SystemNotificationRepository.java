package dinhgiang.dev.hungthinh.repositories;

import dinhgiang.dev.hungthinh.models.entities.bases.SystemNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository qu?n lý truy xu?t d? li?u cho SystemNotification.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface SystemNotificationRepository extends JpaRepository<SystemNotification, Long> {
    Page<SystemNotification> findByResidentId(Long residentId, Pageable pageable);

    List<SystemNotification> findByResidentIdAndIsReadFalse(Long residentId);

    long countByResidentIdAndIsReadFalse(Long residentId);
}
