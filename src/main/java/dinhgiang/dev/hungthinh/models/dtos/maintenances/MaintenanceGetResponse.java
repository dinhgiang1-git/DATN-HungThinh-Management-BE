package dinhgiang.dev.hungthinh.models.dtos.maintenances;

import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentShortGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.devices.DeviceShortGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.users.TechnicianShortGetResponse;
import dinhgiang.dev.hungthinh.models.entities.enums.MaintenanceStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class MaintenanceGetResponse {

    private Long maintenanceId;
    private LocalDate completedDate;
    private LocalDate startedDate;
    private BigDecimal cost;
    private String description;
    private MaintenanceStatus maintenanceStatus;

    private DeviceShortGetResponse device;
    private ApartmentShortGetResponse apartment;
    private List<TechnicianShortGetResponse> technician;
    private dinhgiang.dev.hungthinh.models.dtos.feedbacks.FeedbackShortGetResponse feedback;
}


