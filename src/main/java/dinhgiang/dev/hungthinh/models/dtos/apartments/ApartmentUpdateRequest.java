package dinhgiang.dev.hungthinh.models.dtos.apartments;

import dinhgiang.dev.hungthinh.models.entities.enums.ApartmentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ApartmentUpdateRequest {
    private String complexName;
    private String apartmentNumber;
    private Integer floor;
    private String block;
    private BigDecimal area;
    private Long ownerId;
    private List<Long> residentIds;
    private ApartmentStatus apartmentStatus;
}
