package dinhgiang.dev.hungthinh.repositories;

import dinhgiang.dev.hungthinh.models.entities.bases.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository qu?n lý truy xu?t d? li?u cho AuditLog.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    long countByActionAndCreatedAtBetween(String action, LocalDateTime from, LocalDateTime to);

    @Query("select count(distinct a.performedBy) from AuditLog a where a.createdAt between :from and :to")
    long countDistinctPerformedByBetween(LocalDateTime from, LocalDateTime to);

    @Query("select a.action, count(a) from AuditLog a where a.createdAt between :from and :to group by a.action")
    List<Object[]> countByActionBetween(LocalDateTime from, LocalDateTime to);

    @Query("select a.entityType, count(a) from AuditLog a where a.createdAt between :from and :to group by a.entityType")
    List<Object[]> countByEntityTypeBetween(LocalDateTime from, LocalDateTime to);
}
