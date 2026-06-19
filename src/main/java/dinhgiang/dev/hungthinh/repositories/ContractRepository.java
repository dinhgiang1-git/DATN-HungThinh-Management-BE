package dinhgiang.dev.hungthinh.repositories;

import dinhgiang.dev.hungthinh.models.entities.bases.Contract;
import dinhgiang.dev.hungthinh.models.entities.enums.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository qu?n lý truy xu?t d? li?u cho Contract.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface ContractRepository extends JpaRepository<Contract, Long>, JpaSpecificationExecutor<Contract> {
    List<Contract> findByContractStatusAndEndDateBefore(ContractStatus status, LocalDate date);
    boolean existsByResidentIdAndContractStatus(Long residentId, ContractStatus status);
    boolean existsByContractNumber(String contractNumber);
    long countByContractNumberStartingWith(String prefix);

    @Query("""
            select count(c)
            from Contract c
            where c.apartment.id = :apartmentId
              and c.contractStatus = :status
              and (:contractId is null or c.id <> :contractId)
              and (:endDate is null or c.startDate <= :endDate)
              and (c.endDate is null or c.endDate >= :startDate)
            """)
    long countOverlappingActiveContracts(
            @Param("apartmentId") Long apartmentId,
            @Param("status") ContractStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("contractId") Long contractId
    );

    @Query("""
            select c
            from Contract c
            where c.apartment.id = :apartmentId
              and c.contractStatus = :status
              and c.startDate <= :date
              and (c.endDate is null or c.endDate >= :date)
            order by c.startDate desc, c.id desc
            """)
    List<Contract> findActiveContractsForApartmentAtDate(
            @Param("apartmentId") Long apartmentId,
            @Param("status") ContractStatus status,
            @Param("date") LocalDate date
    );

    @Query("""
            select c
            from Contract c
            left join fetch c.resident
            where c.apartment.id = :apartmentId
            order by c.startDate desc, c.id desc
            """)
    List<Contract> findByApartmentForAdminChat(@Param("apartmentId") Long apartmentId);
}
