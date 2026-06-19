package dinhgiang.dev.hungthinh.models.dtos.apartments;

import dinhgiang.dev.hungthinh.models.dtos.contracts.ContractShortGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.residents.ResidentShortGetResponse;
import dinhgiang.dev.hungthinh.models.entities.enums.ApartmentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ApartmentGetResponse {
    private Long id;
    private String complexName;
    private String apartmentNumber;
    private Integer floor;
    private String block;
    private BigDecimal area;
    private Long ownerId;
    private List<ResidentShortGetResponse> residents;
    private List<ContractShortGetResponse> contracts;
    private ApartmentStatus apartmentStatus;
    private Integer deviceCount;
}
