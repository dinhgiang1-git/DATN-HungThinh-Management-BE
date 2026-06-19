package dinhgiang.dev.hungthinh.models.dtos.invoices;

import dinhgiang.dev.hungthinh.models.entities.enums.MeterReadingSource;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceCreateRequest {
    @NotBlank(message = "Số hóa đơn không được để trống")
    private String invoiceNumber;
    @NotBlank(message = "Ngày hết hạn không được để trống")
    private LocalDate dueDate;

    private BigDecimal electricFee;
    private BigDecimal waterFee;
    private BigDecimal electricPreviousReading;
    private BigDecimal electricCurrentReading;
    private BigDecimal electricQuantity;
    private BigDecimal waterPreviousReading;
    private BigDecimal waterCurrentReading;
    private BigDecimal waterQuantity;
    private MeterReadingSource meterReadingSource;
    private MeterReadingSource electricMeterReadingSource;
    private MeterReadingSource waterMeterReadingSource;
    private BigDecimal managementFee;
    private BigDecimal parkingFee;
    private BigDecimal otherFee;

    private String descriptionOtherFee;

    @NotBlank(message = "Người tạo không được để trống")
    private Long creatorId;
    @NotBlank(message = "Căn hộ không được để trống")
    private Long apartmentId;
}
