package dinhgiang.dev.hungthinh.models.dtos.tableFees;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TableFeeCreateRequest {
    private String title;
    private BigDecimal electricFee;
    private BigDecimal waterFee;
    private BigDecimal managementFee;
    private BigDecimal parkingFee;
    private BigDecimal motorbikeParkingFee;
    private BigDecimal carParkingFee;
    private BigDecimal bicycleParkingFee;
    private BigDecimal electricMotorbikeParkingFee;
    private BigDecimal otherFee;
    private String descriptionOtherFee;
    private Boolean useTieredElectric;
}
