package dinhgiang.dev.hungthinh.models.dtos.contracts;

import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentShortGetResponse;
import dinhgiang.dev.hungthinh.models.entities.enums.ContractStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.ContractType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ContractGetResponse {
    private Long contractId;
    private String contractNumber;
    private ContractType contractType;
    private ContractStatus contractStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private String originalFileName;
    private String note;
    private LocalDateTime createdAt;

    private ApartmentShortGetResponse apartment;
    private ContractResidentResponse resident;

    @Data
    @Builder
    public static class ContractResidentResponse {
        private Long residentId;
        private String fullName;
        private String phoneNumber;
        private String email;
        private String relationship;
    }
}
