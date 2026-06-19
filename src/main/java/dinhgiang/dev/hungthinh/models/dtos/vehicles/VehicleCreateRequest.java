package dinhgiang.dev.hungthinh.models.dtos.vehicles;

import dinhgiang.dev.hungthinh.models.entities.enums.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VehicleCreateRequest {
    @NotBlank(message = "Biển số không được để trống")
    private String licensePlate;
    @NotNull(message = "Loại xe không được để trống")
    private VehicleType vehicleType;
    private String vehicleName;
    @NotNull(message = "Căn hộ không được để trống")
    private Long apartmentId;
}
