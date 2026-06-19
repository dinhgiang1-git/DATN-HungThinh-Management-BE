package dinhgiang.dev.hungthinh.models.dtos.devices;

import dinhgiang.dev.hungthinh.models.entities.enums.DeviceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DeviceUpdateRequest {
    private String deviceName;
    private LocalDate installationDate;
    private String location;
    private Integer maintenanceCycleDay;
    private DeviceStatus deviceStatus;
}
