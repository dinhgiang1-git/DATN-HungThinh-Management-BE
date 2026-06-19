package dinhgiang.dev.hungthinh.models.dtos.tableElectricTiers;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Builder
@Data
public class TableElectricTierGetResponse {
    private Long id;
    private Integer tierOrder; //Thứ tự bậc 1, 2 ,3 ....
    private Integer limitValue;
    private BigDecimal unitPrice; //Đơn giá VND/KWh
}
