package dinhgiang.dev.hungthinh.repositories;

import dinhgiang.dev.hungthinh.models.entities.bases.UtilityMeterReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository qu?n lý truy xu?t d? li?u cho UtilityMeterReading.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface UtilityMeterReadingRepository extends JpaRepository<UtilityMeterReading, Long>, JpaSpecificationExecutor<UtilityMeterReading> {
    Optional<UtilityMeterReading> findTopByApartment_IdAndElectricCurrentReadingIsNotNullOrderByCreatedAtDescIdDesc(Long apartmentId);
    Optional<UtilityMeterReading> findTopByApartment_IdAndWaterCurrentReadingIsNotNullOrderByCreatedAtDescIdDesc(Long apartmentId);
    Optional<UtilityMeterReading> findTopByApartment_IdAndBillingPeriodLessThanAndElectricCurrentReadingIsNotNullOrderByBillingPeriodDescIdDesc(Long apartmentId, String billingPeriod);
    Optional<UtilityMeterReading> findTopByApartment_IdAndBillingPeriodLessThanAndWaterCurrentReadingIsNotNullOrderByBillingPeriodDescIdDesc(Long apartmentId, String billingPeriod);
    boolean existsByApartment_IdAndBillingPeriod(Long apartmentId, String billingPeriod);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update UtilityMeterReading r set r.invoice = null where r.invoice.id = :invoiceId")
    int unlinkInvoice(@Param("invoiceId") Long invoiceId);
}
