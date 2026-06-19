package dinhgiang.dev.hungthinh.models.entities.bases;

import dinhgiang.dev.hungthinh.models.entities.enums.RelationshipType;
import dinhgiang.dev.hungthinh.models.entities.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

/**
 * Entity đại diện cho cư dân trong hệ thống chung cư Hưng Thịnh.
 * Lưu trữ thông tin cá nhân, tài khoản đăng nhập và mối quan hệ với căn hộ.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Builder
@Entity
@Table(name = "residents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Resident extends BaseEntity {

    /** Tên đăng nhập (duy nhất) */
    @Column(name = "user_name", unique = true)
    private String username;

    /** Mật khẩu đã mã hóa */
    @Column(name = "password")
    private String password;

    /** Vai trò trong hệ thống: ADMIN, RESIDENT, TECHNICIAN */
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    // ==================== Thông tin cá nhân ====================

    /** Họ và tên đầy đủ */
    @Column(name = "full_name")
    private String fullName;

    /** Số điện thoại liên hệ */
    @Column(name = "phone_number")
    private String phoneNumber;

    /** Địa chỉ email (duy nhất) */
    @Column(name = "email", unique = true)
    private String email;

    // ==================== Quan hệ (Relationships) ====================

    /** Mối quan hệ với chủ hộ: OWNER, SPOUSE, CHILD, PARENT, RELATIVE, TENANT, OTHER */
    @Column(name = "relationship", nullable = false)
    @Enumerated(EnumType.STRING)
    private RelationshipType relationship;

    /** Căn hộ mà cư dân thuộc về (quan hệ N-1) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id")
    private Apartment apartment;

}
