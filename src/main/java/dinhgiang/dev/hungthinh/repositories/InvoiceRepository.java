package dinhgiang.dev.hungthinh.repositories;

import dinhgiang.dev.hungthinh.models.entities.bases.Invoice;
import dinhgiang.dev.hungthinh.models.entities.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository qu?n lý truy xu?t d? li?u cho Invoice.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface InvoiceRepository extends JpaRepository<Invoice, Long>, JpaSpecificationExecutor<Invoice> {
    Page<Invoice> findByInvoiceStatus(InvoiceStatus invoiceStatus, PageRequest pageRequest);
    List<Invoice> findByInvoiceStatusAndDueDateBefore(InvoiceStatus status, LocalDate date);
    List<Invoice> findByApartment_IdOrderByDueDateDescIdDesc(Long apartmentId, Pageable pageable);
    long countByInvoiceStatus(InvoiceStatus status);
    boolean existsByInvoiceNumber(String invoiceNumber);
    long countByInvoiceNumberStartingWith(String prefix);

    @Query("""
            select i
            from Invoice i
            join fetch i.apartment
            where i.billingPeriod = :billingPeriod
               or (
                    i.billingPeriod is null
                    and i.dueDate >= :periodStart
                    and i.dueDate <= :periodEnd
               )
            order by i.dueDate desc, i.id desc
            """)
    List<Invoice> findForBillingMonth(
            @Param("billingPeriod") String billingPeriod,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd
    );

    @Query("""
            select count(i)
            from Invoice i
            where i.apartment.id = :apartmentId
              and (
                    i.billingPeriod = :billingPeriod
                    or (
                        i.billingPeriod is null
                        and i.dueDate >= :periodStart
                        and i.dueDate <= :periodEnd
                    )
              )
              and (:invoiceId is null or i.id <> :invoiceId)
            """)
    long countExistingInBillingPeriod(
            @Param("apartmentId") Long apartmentId,
            @Param("billingPeriod") String billingPeriod,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd,
            @Param("invoiceId") Long invoiceId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Invoice i where i.id = :id")
    Optional<Invoice> findByIdForUpdate(@Param("id") Long id);
}
