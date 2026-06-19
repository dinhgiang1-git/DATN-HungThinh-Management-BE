package dinhgiang.dev.hungthinh.models.dtos.vehicles;

import dinhgiang.dev.hungthinh.models.entities.enums.VehicleType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VehicleGetResponse {
    private Long vehicleId;
    private String licensePlate;
    private VehicleType vehicleType;
    private String vehicleName;
    private Long apartmentId;
    private String apartmentNumber;
    private String block;
    private Integer floor;
}
