package dinhgiang.dev.hungthinh.repositories;

import dinhgiang.dev.hungthinh.models.entities.bases.Apartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository qu?n lý truy xu?t d? li?u cho Apartment.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface ApartmentRepository extends JpaRepository<Apartment, Long>, JpaSpecificationExecutor<Apartment> {
    boolean existsByApartmentNumber(String apartmentNumber);
    boolean existsByComplexNameIgnoreCaseAndBlockIgnoreCaseAndApartmentNumberIgnoreCase(String complexName, String block, String apartmentNumber);
    List<Apartment> findByBlock(String block);

    @Query("select distinct a.complexName from Apartment a where a.complexName is not null and a.complexName <> '' order by a.complexName")
    List<String> findDistinctComplexNames();

    @Query("select distinct a.block from Apartment a where a.block is not null and a.block <> '' order by a.block")
    List<String> findDistinctBlocks();

    @Query("""
            select a
            from Apartment a
            where lower(a.apartmentNumber) = lower(:apartmentNumber)
              and (:block is null or lower(a.block) = lower(:block))
              and (:complexName is null or lower(a.complexName) = lower(:complexName))
            order by a.complexName, a.block, a.apartmentNumber
            """)
    List<Apartment> findForAdminChat(
            @Param("apartmentNumber") String apartmentNumber,
            @Param("block") String block,
            @Param("complexName") String complexName
    );
}
