package dinhgiang.dev.hungthinh.models.dtos.devices;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeviceShortGetResponse {
    private Long deviceId;
    private String deviceName;
}
