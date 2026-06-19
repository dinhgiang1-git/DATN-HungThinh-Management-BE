package dinhgiang.dev.hungthinh.models.dtos.meterreadings;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MeterReadingPreviewResponse {
    private Long apartmentId;
    private String apartmentNumber;
    private String billingPeriod;

    private BigDecimal electricPreviousReading;
    private BigDecimal electricQuantity;
    private BigDecimal electricCurrentReading;

    private BigDecimal waterPreviousReading;
    private BigDecimal waterQuantity;
    private BigDecimal waterCurrentReading;
}
