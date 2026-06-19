package dinhgiang.dev.hungthinh.models.dtos.vehicles;

import dinhgiang.dev.hungthinh.models.entities.enums.VehicleType;
import lombok.Data;

@Data
public class VehicleUpdateRequest {
    private String licensePlate;
    private VehicleType vehicleType;
    private String vehicleName;
}
