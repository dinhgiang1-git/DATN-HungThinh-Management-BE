package dinhgiang.dev.hungthinh.models.dtos.apartments;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ApartmentReportItemResponse {
    private String groupKey;
    private String groupLabel;
    private String complexName;
    private String block;
    private Integer floor;
    private Long apartmentId;
    private String apartmentNumber;

    private long apartmentCount;
    private long occupiedCount;
    private long vacantCount;
    private long maintenanceCount;
    private long residentCount;
    private long deviceCount;
    private long invoiceCount;
    private long unpaidInvoiceCount;
    private BigDecimal totalRevenue;
    private BigDecimal paidRevenue;
    private BigDecimal unpaidRevenue;
}
