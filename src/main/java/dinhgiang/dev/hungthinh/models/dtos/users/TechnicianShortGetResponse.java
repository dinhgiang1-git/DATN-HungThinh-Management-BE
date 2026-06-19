package dinhgiang.dev.hungthinh.models.dtos.users;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TechnicianShortGetResponse {
    private Long technicianId;
    private String technicianName;
}
