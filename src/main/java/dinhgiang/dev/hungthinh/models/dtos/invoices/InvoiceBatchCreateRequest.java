package dinhgiang.dev.hungthinh.models.dtos.invoices;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class InvoiceBatchCreateRequest {
    @NotNull(message = "Danh sách căn hộ không được để trống")
    private List<Long> apartmentIds;
    @NotNull(message = "Hạn thanh toán không được để trống")
    private LocalDate dueDate;
    @NotNull(message = "Người tạo không được để trống")
    private Long creatorId;
    @NotNull(message = "Bảng phí không được để trống")
    private Long tableFeeId;

    private BigDecimal electricFee;
    private BigDecimal waterFee;
    private BigDecimal managementFee;
    private BigDecimal motorbikeParkingFee;
    private BigDecimal carParkingFee;
    private BigDecimal bicycleParkingFee;
    private BigDecimal electricMotorbikeParkingFee;
    private BigDecimal otherFee;
    private String descriptionOtherFee;
    private Boolean useTieredElectric;
}
