package dinhgiang.dev.hungthinh.components;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Đánh dấu method cần ghi audit log.
 * AOP Aspect sẽ tự động ghi log sau khi method thực hiện thành công.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    /** Hành động: CREATE, UPDATE, DELETE */
    String action();

    /** Loại đối tượng: INVOICE, CONTRACT, VEHICLE, APARTMENT, RESIDENT, USER, PAYMENT */
    String entityType();

    /** Mô tả hành động (hỗ trợ SpEL) */
    String description() default "";
}
