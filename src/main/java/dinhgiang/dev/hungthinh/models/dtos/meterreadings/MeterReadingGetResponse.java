package dinhgiang.dev.hungthinh.models.dtos.meterreadings;

import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentShortGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.users.UserShortGetResponse;
import dinhgiang.dev.hungthinh.models.entities.enums.MeterReadingSource;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MeterReadingGetResponse {
    private Long meterReadingId;
    private String billingPeriod;
    private LocalDateTime recordedAt;

    private BigDecimal electricPreviousReading;
    private BigDecimal electricCurrentReading;
    private BigDecimal electricQuantity;
    private BigDecimal waterPreviousReading;
    private BigDecimal waterCurrentReading;
    private BigDecimal waterQuantity;

    private MeterReadingSource source;
    private String evidenceOriginalFileName;
    private String evidenceUrl;
    private String electricEvidenceOriginalFileName;
    private String electricEvidenceUrl;
    private String waterEvidenceOriginalFileName;
    private String waterEvidenceUrl;
    private String note;

    private ApartmentShortGetResponse apartment;
    private UserShortGetResponse recordedBy;
    private Long invoiceId;
}
