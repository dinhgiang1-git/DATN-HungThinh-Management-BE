package dinhgiang.dev.hungthinh.models.dtos.users;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserShortGetResponse {
    private Long userId;
    private String fullName;
    private String phoneNumber;
}
