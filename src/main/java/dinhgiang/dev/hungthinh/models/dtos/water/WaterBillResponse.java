package dinhgiang.dev.hungthinh.models.dtos.water;

import lombok.*;

import java.time.YearMonth;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaterBillResponse {
    private String customerName;
    private String address;
    private YearMonth billingPeriod; //Kỳ hóa đơn: 2026/04
    private Integer cubicMeterConsumed; // Số m³ nước tiêu thụ
    private String status; //PAID hoặc UNPAID
}
