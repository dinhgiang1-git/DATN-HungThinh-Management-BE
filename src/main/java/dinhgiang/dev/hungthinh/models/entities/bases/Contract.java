package dinhgiang.dev.hungthinh.models.entities.bases;

import dinhgiang.dev.hungthinh.models.entities.enums.ContractStatus;
import dinhgiang.dev.hungthinh.models.entities.enums.ContractType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Entity đại diện cho hợp đồng trong hệ thống chung cư Hưng Thịnh.
 * Lưu trữ thông tin hợp đồng: số hợp đồng, loại, trạng thái, thời hạn và file đính kèm.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Entity
@Setter
@Getter
@Table(name = "contracts")
public class Contract extends BaseEntity {

    /** Số hợp đồng (duy nhất) */
    @Column(name = "contract_number", nullable = false, unique = true)
    private String contractNumber;

    /** Loại hợp đồng: RENT, PURCHASE, SERVICE */
    @Column(name = "contract_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContractType contractType;

    /** Trạng thái hợp đồng: ACTIVE, EXPIRED, TERMINATED */
    @Column(name = "contract_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContractStatus contractStatus;

    /** Ngày bắt đầu hiệu lực */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /** Ngày kết thúc hiệu lực */
    @Column(name = "end_date")
    private LocalDate endDate;

    /** Đường dẫn file hợp đồng trên server */
    @Column(name = "file_path")
    private String filePath;

    /** Tên file gốc khi upload */
    @Column(name = "original_file_name")
    private String originalFileName;

    /** Ghi chú bổ sung */
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // ==================== Quan hệ (Relationships) ====================

    /** Căn hộ liên quan đến hợp đồng (quan hệ N-1) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartment_id")
    private Apartment apartment;

    /** Cư dân ký hợp đồng (quan hệ N-1) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id")
    private Resident resident;

}
