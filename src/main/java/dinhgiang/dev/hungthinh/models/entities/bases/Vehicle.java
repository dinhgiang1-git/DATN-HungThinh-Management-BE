package dinhgiang.dev.hungthinh.models.entities.bases;

import dinhgiang.dev.hungthinh.models.entities.enums.VehicleType;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity đại diện cho phương tiện đăng ký gửi xe tại chung cư.
 * Lưu trữ thông tin: biển số, loại xe, tên xe và căn hộ sở hữu.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle extends BaseEntity {

    /** Biển số xe (duy nhất) */
    @Column(name = "license_plate", nullable = false, unique = true)
    private String licensePlate;

    /** Loại phương tiện: MOTORBIKE, CAR, BICYCLE, ELECTRIC_MOTORBIKE */
    @Column(name = "vehicle_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;

    /** Tên/mô tả phương tiện */
    @Column(name = "vehicle_name")
    private String vehicleName;

    // ==================== Quan hệ (Relationships) ====================

    /** Căn hộ sở hữu phương tiện (quan hệ N-1) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;
}
