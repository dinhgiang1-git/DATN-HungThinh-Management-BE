package dinhgiang.dev.hungthinh.models.dtos.invoices;

import dinhgiang.dev.hungthinh.models.entities.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Data
public class InvoiceUpdateRequest {

    private String invoiceNumber;
    private LocalDate DueDate;

    private BigDecimal electricFee;
    private BigDecimal waterFee;
    private BigDecimal electricPreviousReading;
    private BigDecimal electricCurrentReading;
    private BigDecimal electricQuantity;
    private BigDecimal waterPreviousReading;
    private BigDecimal waterCurrentReading;
    private BigDecimal waterQuantity;
    private BigDecimal managementFee;
    private BigDecimal parkingFee;
    private BigDecimal otherFee;
    private BigDecimal totalAmount;

    private String descriptionOtherFee;

    @Column(name = "invoice_status")
    @Enumerated(EnumType.STRING)
    private InvoiceStatus invoiceStatus;
}
