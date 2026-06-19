package dinhgiang.dev.hungthinh.models.dtos.maintenances;

import dinhgiang.dev.hungthinh.models.entities.enums.MaintenanceStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class MaintenanceUpdateRequest {

    private LocalDate completedDate;

    private LocalDate startedDate;

    private BigDecimal cost;

    private String description;

    private MaintenanceStatus maintenanceStatus;

    private Long deviceId;

    private List<Long> technicianId;
}
