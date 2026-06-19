package dinhgiang.dev.hungthinh.models.dtos.devices;

import dinhgiang.dev.hungthinh.models.entities.enums.DeviceStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DeviceCreateRequest {
    @NotBlank(message = "Tên thiết bị không được để trống")
    private String deviceName;
    private LocalDate installationDate;
    @NotBlank(message = "Vị trí lắp đặt không được để trống")
    private String location;
    private Integer maintenanceCycleDay;
    @NotNull(message = "Trạng thái thiết bị không được để trống")
    private DeviceStatus deviceStatus;
    private Long apartmentId;
    @NotNull(message = "Loại thiết bị không được để trống")
    private DeviceType deviceType;
}
