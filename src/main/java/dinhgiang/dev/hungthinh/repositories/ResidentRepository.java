package dinhgiang.dev.hungthinh.repositories;

import dinhgiang.dev.hungthinh.models.entities.bases.Resident;
import dinhgiang.dev.hungthinh.models.entities.enums.RelationshipType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

/**
 * Repository qu?n lý truy xu?t d? li?u cho Resident.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface ResidentRepository extends JpaRepository<Resident, Long>, JpaSpecificationExecutor<Resident> {
    List<Resident> findByRelationship(RelationshipType relationship);
    List<Resident> findByApartment_Id(Long apartmentId);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<Resident> findByUsername(String username);
}
