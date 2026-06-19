package dinhgiang.dev.hungthinh.repositories;

import dinhgiang.dev.hungthinh.models.entities.bases.Maintenance;
import dinhgiang.dev.hungthinh.models.entities.enums.MaintenanceStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

/**
 * Repository qu?n lý truy xu?t d? li?u cho Maintenance.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface MaintenanceRepository extends JpaRepository<Maintenance, Long>, JpaSpecificationExecutor<Maintenance> {
    @Query("""
            select m
            from Maintenance m
            left join fetch m.apartment
            left join fetch m.device
            where m.maintenanceStatus in :statuses
            order by m.startedDate asc, m.id desc
            """)
    List<Maintenance> findOpenIssuesForAdminChat(@Param("statuses") Collection<MaintenanceStatus> statuses, Pageable pageable);

    @Query("""
            select m
            from Maintenance m
            left join fetch m.apartment
            left join fetch m.device
            where m.apartment.id = :apartmentId
            order by m.startedDate desc, m.id desc
            """)
    List<Maintenance> findByApartmentForAdminChat(@Param("apartmentId") Long apartmentId, Pageable pageable);
}
