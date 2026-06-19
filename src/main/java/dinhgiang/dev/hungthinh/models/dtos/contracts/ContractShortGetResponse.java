package dinhgiang.dev.hungthinh.models.dtos.contracts;

import dinhgiang.dev.hungthinh.models.entities.enums.ContractStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.ContractType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ContractShortGetResponse {
    private Long contractId;
    private String contractNumber;
    private ContractType contractType;
    private ContractStatus contractStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private String residentName;
    private String originalFileName;
}
