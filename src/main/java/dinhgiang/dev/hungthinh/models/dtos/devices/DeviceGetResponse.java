package dinhgiang.dev.hungthinh.models.dtos.devices;

import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentShortGetResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import dinhgiang.dev.hungthinh.models.entities.enums.DeviceStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.DeviceType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DeviceGetResponse {
    private Long id;
    private String deviceName;
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate installationDate;
    private String location;
    private Integer maintenanceCycleDay;
    private DeviceStatus deviceStatus;
    private ApartmentShortGetResponse apartment;
    private DeviceType deviceType;
}
