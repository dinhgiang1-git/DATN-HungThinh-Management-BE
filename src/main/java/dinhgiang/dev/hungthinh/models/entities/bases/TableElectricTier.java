package dinhgiang.dev.hungthinh.models.entities.bases;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * Entity đại diện cho bậc thang giá điện.
 * Mỗi bậc định nghĩa giới hạn kWh và đơn giá tương ứng,
 * dùng để tính phí điện theo hệ thống luỹ tiến.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "table_electric_tier")
public class TableElectricTier extends BaseEntity{

    /** Thứ tự bậc thang (1, 2, 3, ...) */
    @Column(name = "tier_order", nullable = false)
    private Integer tierOrder;

    /** Giới hạn kWh của bậc (null = không giới hạn, bậc cuối cùng) */
    @Column(name = "limit_value")
    private Integer limitValue;

    /** Đơn giá điện (VND/kWh) */
    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

}
