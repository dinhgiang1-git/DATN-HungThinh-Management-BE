package dinhgiang.dev.hungthinh.models.dtos.evns;

import lombok.*;

import java.time.YearMonth;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvnBillResponse {
    private String customerName;
    private String address;
    private YearMonth billingPeriod; //Kỳ hóa đơn: 2026/04
    private Integer kwhConsumed;
    private String status; //PAID hoặc UNPAID
}
