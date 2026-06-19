package dinhgiang.dev.hungthinh.models.entities.bases;

import dinhgiang.dev.hungthinh.models.entities.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

/**
 * Entity đại diện cho người dùng hệ thống (Admin, Kỹ thuật viên).
 * Lưu trữ thông tin tài khoản, thông tin cá nhân và vai trò trong hệ thống.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Builder
@Data
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    /** Tên đăng nhập (duy nhất, bắt buộc) */
    @Column(name = "user_name", unique = true, nullable = false)
    private String username;

    /** Mật khẩu đã mã hóa */
    @Column(name = "password", nullable = false)
    private String password;

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

    /** Vai trò: ADMIN, RESIDENT, TECHNICIAN */
    @Column(name = "user_role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    /** Danh sách công việc bảo trì được phân công (quan hệ N-N) */
    @ManyToMany(mappedBy = "technicians")
    private List<Maintenance> maintenances;

    /** Danh sách hóa đơn do user tạo (quan hệ 1-N) */
    @OneToMany(mappedBy = "creator")
    private List<Invoice> invoices;

}
