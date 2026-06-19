package dinhgiang.dev.hungthinh.services.implement;

import dinhgiang.dev.hungthinh.exceptions.UserMessageException;
import dinhgiang.dev.hungthinh.models.dtos.vehicles.VehicleCreateRequest;
import dinhgiang.dev.hungthinh.models.dtos.vehicles.VehicleGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.vehicles.VehicleUpdateRequest;
import dinhgiang.dev.hungthinh.models.entities.bases.Apartment;
import dinhgiang.dev.hungthinh.models.entities.bases.Vehicle;
import dinhgiang.dev.hungthinh.models.entities.enums.VehicleType;
import dinhgiang.dev.hungthinh.repositories.ApartmentRepository;
import dinhgiang.dev.hungthinh.repositories.VehicleRepository;
import dinhgiang.dev.hungthinh.components.Auditable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * Lớp dịch vụ quản lý Phương tiện (Vehicle).
 * Thêm, sửa, xóa các phương tiện đăng ký gửi xe của cư dân.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final ApartmentRepository apartmentRepository;
    private final AccessControlService accessControlService;

    public List<VehicleGetResponse> getVehiclesByApartmentId(Long apartmentId) {
        accessControlService.assertResidentCanAccessApartment(
                apartmentId,
                "Bạn không có quyền xem phương tiện của căn hộ này"
        );
        return vehicleRepository.findByApartmentId(apartmentId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<VehicleGetResponse> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public VehicleGetResponse getVehicleById(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new UserMessageException("Phương tiện không tồn tại"));
        assertResidentCanAccessVehicle(vehicle);
        return mapToResponse(vehicle);
    }

    @Auditable(action = "CREATE", entityType = "VEHICLE")
    public Long createVehicle(VehicleCreateRequest request) {
        if (vehicleRepository.existsByLicensePlate(request.getLicensePlate())) {
            throw new UserMessageException("Biển số " + request.getLicensePlate() + " đã tồn tại trong hệ thống");
        }
        Apartment apartment = apartmentRepository.findById(request.getApartmentId())
                .orElseThrow(() -> new UserMessageException("Căn hộ không tồn tại"));
        accessControlService.assertResidentCanAccessApartment(
                apartment,
                "Bạn không có quyền thêm phương tiện cho căn hộ này"
        );

        Vehicle vehicle = Vehicle.builder()
                .licensePlate(request.getLicensePlate().toUpperCase().trim())
                .vehicleType(request.getVehicleType())
                .vehicleName(request.getVehicleName())
                .apartment(apartment)
                .build();
        vehicleRepository.save(vehicle);
        return vehicle.getId();
    }

    @Auditable(action = "UPDATE", entityType = "VEHICLE")
    public Long updateVehicle(Long vehicleId, VehicleUpdateRequest request) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new UserMessageException("Phương tiện không tồn tại"));
        assertResidentCanAccessVehicle(vehicle);

        if (request.getLicensePlate() != null) {
            String newPlate = request.getLicensePlate().toUpperCase().trim();
            if (!newPlate.equals(vehicle.getLicensePlate()) && vehicleRepository.existsByLicensePlate(newPlate)) {
                throw new UserMessageException("Biển số " + newPlate + " đã tồn tại trong hệ thống");
            }
            vehicle.setLicensePlate(newPlate);
        }
        if (request.getVehicleType() != null) {
            vehicle.setVehicleType(request.getVehicleType());
        }
        if (request.getVehicleName() != null) {
            vehicle.setVehicleName(request.getVehicleName());
        }
        vehicleRepository.save(vehicle);
        return vehicle.getId();
    }

    @Auditable(action = "DELETE", entityType = "VEHICLE")
    public Void deleteVehicle(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new UserMessageException("Phương tiện không tồn tại"));
        assertResidentCanAccessVehicle(vehicle);
        vehicleRepository.delete(vehicle);
        return null;
    }

    /**
     * Tính phí gửi xe cho 1 căn hộ dựa trên đơn giá
     */
    public BigDecimal calculateParkingFee(Long apartmentId, BigDecimal motorbikeFee, BigDecimal carFee, BigDecimal bicycleFee, BigDecimal electricMotorbikeFee) {
        long motorbikeCount = vehicleRepository.countByApartmentIdAndVehicleType(apartmentId, VehicleType.MOTORBIKE);
        long carCount = vehicleRepository.countByApartmentIdAndVehicleType(apartmentId, VehicleType.CAR);
        long bicycleCount = vehicleRepository.countByApartmentIdAndVehicleType(apartmentId, VehicleType.BICYCLE);
        long electricMotorbikeCount = vehicleRepository.countByApartmentIdAndVehicleType(apartmentId, VehicleType.ELECTRIC_MOTORBIKE);
        
        BigDecimal total = BigDecimal.ZERO;
        if (motorbikeFee != null) {
            total = total.add(motorbikeFee.multiply(BigDecimal.valueOf(motorbikeCount)));
        }
        if (carFee != null) {
            total = total.add(carFee.multiply(BigDecimal.valueOf(carCount)));
        }
        if (bicycleFee != null) {
            total = total.add(bicycleFee.multiply(BigDecimal.valueOf(bicycleCount)));
        }
        if (electricMotorbikeFee != null) {
            total = total.add(electricMotorbikeFee.multiply(BigDecimal.valueOf(electricMotorbikeCount)));
        }
        return total;
    }

    private VehicleGetResponse mapToResponse(Vehicle vehicle) {
        return VehicleGetResponse.builder()
                .vehicleId(vehicle.getId())
                .licensePlate(vehicle.getLicensePlate())
                .vehicleType(vehicle.getVehicleType())
                .vehicleName(vehicle.getVehicleName())
                .apartmentId(vehicle.getApartment().getId())
                .apartmentNumber(vehicle.getApartment().getApartmentNumber())
                .block(vehicle.getApartment().getBlock())
                .floor(vehicle.getApartment().getFloor())
                .build();
    }

    private void assertResidentCanAccessVehicle(Vehicle vehicle) {
        accessControlService.assertResidentCanAccessApartment(
                vehicle.getApartment(),
                "Bạn không có quyền truy cập phương tiện này"
        );
    }
}
