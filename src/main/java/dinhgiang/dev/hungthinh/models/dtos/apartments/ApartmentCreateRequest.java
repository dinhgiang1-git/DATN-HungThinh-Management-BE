package dinhgiang.dev.hungthinh.models.dtos.apartments;
import dinhgiang.dev.hungthinh.models.entities.enums.ApartmentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ApartmentCreateRequest {
    private String complexName;
    @NotBlank(message = "Số nhà không được để trống")
    private String apartmentNumber;
    @NotNull(message = "Số tầng không được để trống")
    private Integer floor;
    @NotBlank(message = "Số tòa không được để trống")
    private String block;
    private BigDecimal area;
    private Long ownerId;
    private List<Long> residentIds;
    @NotNull(message = "Trạng thái tòa nhà không được để trống")
    private ApartmentStatus apartmentStatus;
}
