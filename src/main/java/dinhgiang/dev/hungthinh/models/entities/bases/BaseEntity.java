package dinhgiang.dev.hungthinh.models.entities.bases;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Lớp Entity cơ sở cho tất cả các thực thể JPA trong hệ thống.
 * Cung cấp các trường chung: id, createdAt, modifiedAt và các phương thức equals/hashCode.
 *
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Getter
@Setter
@MappedSuperclass
public class BaseEntity {

    /** Khóa chính tự động tăng */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    @EqualsAndHashCode.Include
    private Long id;

    /** Thời gian tạo bản ghi (tự động gán khi tạo) */
    @Column(name = "created_at", updatable = false, nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt =  LocalDateTime.now();

    /** Thời gian cập nhật gần nhất (tự động cập nhật) */
    @Column(name = "modified_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime modifiedAt;

    /**
     * So sánh hai entity dựa trên ID.
     *
     * @param o đối tượng cần so sánh
     * @return true nếu cùng class và cùng ID
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        return id != null && id.equals(that.id);
    }

    /**
     * Trả về hashCode cố định để đảm bảo tính nhất quán với JPA proxy.
     *
     * @return giá trị hashCode cố định
     */
    @Override
    public int hashCode() {
        return 31;
    }

}
