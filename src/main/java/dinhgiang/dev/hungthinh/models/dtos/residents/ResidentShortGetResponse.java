package dinhgiang.dev.hungthinh.models.dtos.residents;

import dinhgiang.dev.hungthinh.models.entities.enums.RelationshipType;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ResidentShortGetResponse {
    private Long residentId;
    private String fullName;
    private String email;
    private String phone;
    private RelationshipType relationshipType;
}
