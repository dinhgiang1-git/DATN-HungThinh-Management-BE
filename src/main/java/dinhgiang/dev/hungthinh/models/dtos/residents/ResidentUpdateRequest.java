package dinhgiang.dev.hungthinh.models.dtos.residents;

import dinhgiang.dev.hungthinh.models.entities.enums.RelationshipType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResidentUpdateRequest {

    private String fullName;

    private String phoneNumber;

    private String email;

    private RelationshipType relationship;
}
