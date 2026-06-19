package dinhgiang.dev.hungthinh.models.dtos.apartments;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ApartmentShortGetResponse {
    private Long id;
    private String complexName;
    private String apartmentNumber;
    private Integer floor;
    private String block;
    private Long ownerId;
    private Integer deviceCount;
}
