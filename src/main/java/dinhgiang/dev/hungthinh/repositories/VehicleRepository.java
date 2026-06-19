package dinhgiang.dev.hungthinh.repositories;

import dinhgiang.dev.hungthinh.models.entities.bases.Vehicle;
import dinhgiang.dev.hungthinh.models.entities.enums.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository qu?n lý truy xu?t d? li?u cho Vehicle.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByApartmentId(Long apartmentId);
    boolean existsByLicensePlate(String licensePlate);
    long countByApartmentIdAndVehicleType(Long apartmentId, VehicleType vehicleType);
}
