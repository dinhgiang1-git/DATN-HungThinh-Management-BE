package dinhgiang.dev.hungthinh.models.dtos.apartments;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ApartmentStatisticsResponse {
    private String groupBy;
    private String complexName;
    private String block;
    private Integer floor;

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

    private List<ApartmentReportItemResponse> items;
}
