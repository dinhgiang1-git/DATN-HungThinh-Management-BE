package dinhgiang.dev.hungthinh.models.entities.bases;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity đại diện cho bảng phí dịch vụ chung cư.
 * Cấu hình đơn giá cho tất cả các loại phí: điện, nước, quản lý, gửi xe theo từng loại phương tiện.
 * Hỗ trợ chế độ tính phí điện theo bậc thang hoặc đơn giá cố định.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "table_fee")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableFee extends BaseEntity{

    /** Tiêu đề bảng phí */
    @Column(name = "title")
    private String title;

    /** Đơn giá điện cố định (VND/kWh) - dùng khi useTieredElectric = false */
    @Column(name = "electric_fee")
    private BigDecimal electricFee;

    /** Đơn giá nước (VND/m³) */
    @Column(name = "water_fee")
    private BigDecimal waterFee;

    /** Phí quản lý chung cư (VND/tháng) */
    @Column(name = "management_fee")
    private BigDecimal managementFee;

    /** Phí gửi xe tổng (VND/tháng) - legacy field */
    @Column(name = "parking_fee")
    private BigDecimal parkingFee;

    /** Phí gửi xe máy (VND/xe/tháng) */
    @Column(name = "motorbike_parking_fee")
    private BigDecimal motorbikeParkingFee;

    /** Phí gửi ô tô (VND/xe/tháng) */
    @Column(name = "car_parking_fee")
    private BigDecimal carParkingFee;

    /** Phí gửi xe đạp (VND/xe/tháng) */
    @Column(name = "bicycle_parking_fee")
    private BigDecimal bicycleParkingFee;

    /** Phí gửi xe máy điện (VND/xe/tháng) */
    @Column(name = "electric_motorbike_parking_fee")
    private BigDecimal electricMotorbikeParkingFee;

    /** Phí khác (VND) */
    @Column(name = "other_fee")
    private BigDecimal otherFee;

    /** Mô tả chi tiết phí khác */
    @Column(name = "description_other_fee", columnDefinition = "TEXT")
    private String descriptionOtherFee;

    /** Cờ sử dụng tính phí điện theo bậc thang (true: bậc thang, false: đơn giá cố định) */
    @Column(name = "use_tiered_electric", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    @Builder.Default
    private Boolean useTieredElectric = false;
}
