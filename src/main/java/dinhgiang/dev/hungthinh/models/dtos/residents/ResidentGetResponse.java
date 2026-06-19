package dinhgiang.dev.hungthinh.models.dtos.residents;

import com.fasterxml.jackson.annotation.JsonInclude;
import dinhgiang.dev.hungthinh.models.entities.enums.RelationshipType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResidentGetResponse {

    private Long id;

    private String userName;

    private String fullName;

    private String phoneNumber;

    private String email;

    private RelationshipType relationship;
}
