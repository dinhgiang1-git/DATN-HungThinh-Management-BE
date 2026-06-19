package dinhgiang.dev.hungthinh.models.dtos.tableFees;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableFeeGetResponse {
    private Long id;
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
