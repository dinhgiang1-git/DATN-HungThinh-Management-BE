package dinhgiang.dev.hungthinh.models.entities.bases;

import dinhgiang.dev.hungthinh.models.entities.enums.DeviceStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.DeviceType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.List;

/**
 * Entity đại diện cho thiết bị trong chung cư Hưng Thịnh.
 * Lưu trữ thông tin thiết bị: tên, vị trí, ngày lắp đặt, chu kỳ bảo trì và trạng thái.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "devices")
public class Device extends BaseEntity {

    /** Tên thiết bị */
    @Column(name = "device_name", nullable = false)
    private String deviceName;

    /** Ngày lắp đặt thiết bị */
    @Column(name = "installation_date", nullable = false)
    private LocalDate installationDate;

    /** Vị trí lắp đặt */
    @Column(name = "location", nullable = false)
    private String location;

    /** Chu kỳ bảo trì (đơn vị: ngày) */
    @Column(name = "maintenance_cycle_day")
    private Integer maintenanceCycleDay;

    /** Trạng thái thiết bị: ACTIVE, INACTIVE, BROKEN, UNDER_MAINTENANCE */
    @Column(name = "device_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private DeviceStatus deviceStatus;

    /** Loại thiết bị */
    @Column(name = "device_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;

    // ==================== Quan hệ (Relationships) ====================

    /** Căn hộ sở hữu thiết bị (quan hệ N-1) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id")
    private Apartment apartment;

    /** Danh sách lịch sử bảo trì thiết bị (quan hệ 1-N) */
    @OneToMany(mappedBy = "device", cascade =  CascadeType.ALL)
    private List<Maintenance> maintenanceList;
}
