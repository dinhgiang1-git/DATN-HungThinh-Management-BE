package dinhgiang.dev.hungthinh.repositories;

import dinhgiang.dev.hungthinh.models.entities.bases.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repository qu?n lý truy xu?t d? li?u cho Device.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
public interface DeviceRepository extends JpaRepository<Device, Long>, JpaSpecificationExecutor<Device> {
    int countByApartmentId(Long apartmentId);
    java.util.List<Device> findByApartmentId(Long apartmentId);
}
