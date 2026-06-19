package dinhgiang.dev.hungthinh.models.entities.bases;

import dinhgiang.dev.hungthinh.models.entities.enums.ApartmentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * Entity đại diện cho căn hộ trong hệ thống chung cư Hưng Thịnh.
 * Lưu trữ thông tin căn hộ: số căn hộ, tầng, block, diện tích và trạng thái.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Entity
@Setter
@Getter
@Table(
        name = "apartments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_apartment_location_number",
                columnNames = {"complex_name", "block", "apartment_number"}
        )
)
public class Apartment extends BaseEntity {

    /** Tên khu chung cư/dự án chứa căn hộ */
    @Column(name = "complex_name")
    private String complexName;

    /** Số căn hộ (duy nhất trong cùng khu và cùng tòa) */
    @Column(name = "apartment_number", nullable = false)
    private String apartmentNumber;

    /** Tầng của căn hộ */
    @Column(name = "floor", nullable = false)
    private Integer floor;

    /** Block (tòa nhà) của căn hộ */
    @Column(name = "block", nullable = false)
    private String block;

    /** Diện tích căn hộ (m²) */
    @Column(name = "area", precision = 8, scale = 2)
    private BigDecimal area;

    /** ID của chủ sở hữu căn hộ (tham chiếu đến Resident) */
    @Column(name = "owner_id")
    private Long ownerId;

    /** Trạng thái căn hộ: OCCUPIED, VACANT, UNDER_MAINTENANCE */
    @Column(name = "apartment_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ApartmentStatus apartmentStatus;

    // ==================== Quan hệ (Relationships) ====================

    /** Danh sách cư dân thuộc căn hộ (quan hệ 1-N) */
    @OneToMany(mappedBy = "apartment", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Resident> residents;

    /** Danh sách phản hồi/góp ý từ căn hộ (quan hệ 1-N) */
    @OneToMany(mappedBy = "apartment", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Feedback> feedbacks;

    /** Danh sách hóa đơn của căn hộ (quan hệ 1-N) */
    @OneToMany(mappedBy = "apartment", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Invoice> invoices;

    /** Danh sách hợp đồng liên quan đến căn hộ (quan hệ 1-N) */
    @OneToMany(mappedBy = "apartment", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Contract> contracts;

    /** Danh sách phương tiện đăng ký của căn hộ (quan hệ 1-N) */
    @OneToMany(mappedBy = "apartment", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Vehicle> vehicles;

    /** Danh sách thiết bị thuộc căn hộ (quan hệ 1-N) */
    @OneToMany(mappedBy = "apartment", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Device> devices;
}
