package dinhgiang.dev.hungthinh.models.dtos.maintenances;

import dinhgiang.dev.hungthinh.models.entities.enums.MaintenanceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class MaintenanceCreateRequest {

    private LocalDate completedDate;
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startedDate;
    private BigDecimal cost;
    @NotBlank(message = "Mô tả không được để trống")
    private String description;
    @NotNull(message = "Trạng thái bảo trì không được để trống")
    private MaintenanceStatus maintenanceStatus;

    private Long deviceId;
    @NotNull(message = "Thiếu kĩ thuật viên")
    private List<Long> technicianId;

    private Long feedbackId;
    private Long apartmentId;
}
