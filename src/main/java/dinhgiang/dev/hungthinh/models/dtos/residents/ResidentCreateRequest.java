package dinhgiang.dev.hungthinh.models.dtos.residents;

import dinhgiang.dev.hungthinh.models.entities.enums.RelationshipType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResidentCreateRequest {
    private String userName;
    private String password;
    @NotBlank(message = "Tên không được để trống")
    private String fullName;
    @NotBlank(message = "Số điện thoại không được để trống")
    private String phoneNumber;
    private String email;
    @NotNull(message = "Mối quan hệ không được để trống")
    private RelationshipType relationship;
    private Long apartmentId;
}
