package dinhgiang.dev.hungthinh.models.dtos.invoices;

import dinhgiang.dev.hungthinh.models.dtos.apartments.ApartmentShortGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.payments.PaymentGetResponse;
import dinhgiang.dev.hungthinh.models.dtos.users.UserShortGetResponse;
import dinhgiang.dev.hungthinh.models.entities.enums.InvoiceStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class InvoiceGetResponse {

    private Long invoiceId;
    private String invoiceNumber;
    private LocalDate DueDate;
    private String billingPeriod;
    private LocalDateTime createdAt;

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

    private InvoiceStatus invoiceStatus;

    private UserShortGetResponse creator;
    private ApartmentShortGetResponse apartment;
    private List<PaymentGetResponse> payments;
}
