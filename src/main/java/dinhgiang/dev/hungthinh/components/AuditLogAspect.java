package dinhgiang.dev.hungthinh.components;

import dinhgiang.dev.hungthinh.services.implement.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

/**
 * AOP Aspect tự động ghi audit log cho các method có @Auditable.
 * Ghi log SAU KHI method thực hiện thành công (AfterReturning).
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogAspect {

    private final AuditLogService auditLogService;

    @AfterReturning(pointcut = "@annotation(auditable)", returning = "result")
    public void logAudit(JoinPoint joinPoint, Auditable auditable, Object result) {
        try {
            String action = auditable.action();
            String entityType = auditable.entityType();
            String description = auditable.description();

            Long entityId = extractEntityId(result, joinPoint);
            String entityName = extractEntityName(joinPoint, entityType);
            String details = buildDetails(description, action, entityType, joinPoint, entityId);

            auditLogService.log(action, entityType, entityId, entityName, details);
            AuditLogContext.markLogged();
        } catch (Exception e) {
            // Không để lỗi audit ảnh hưởng đến business logic
            log.warn("Audit log failed: {}", e.getMessage());
        }
    }

    /**
     * Trích xuất entity ID từ kết quả trả về hoặc tham số
     */
    private Long extractEntityId(Object result, JoinPoint joinPoint) {
        // Nếu result là Long (create/update trả về ID)
        if (result instanceof Long id) {
            return id;
        }
        // Nếu result là List<Long> (batch create)
        if (result instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Long) {
            return null; // Batch → không có 1 ID cụ thể
        }
        // Thử lấy từ tham số đầu tiên (delete, getById)
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof Long id) {
            return id;
        }
        return null;
    }

    /**
     * Trích xuất tên hiển thị từ tham số (nếu có)
     */
    private String extractEntityName(JoinPoint joinPoint, String entityType) {
        Object[] args = joinPoint.getArgs();
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = sig.getParameterNames();

        if (paramNames == null) return null;

        // Cố gắng tìm request object → lấy trường đặc trưng
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) continue;

            try {
                // Thử lấy trường licensePlate (Vehicle)
                Method m = arg.getClass().getMethod("getLicensePlate");
                Object val = m.invoke(arg);
                if (val != null) return val.toString();
            } catch (Exception ignored) {}

            try {
                // Thử lấy trường apartmentNumber (Apartment)
                Method m = arg.getClass().getMethod("getApartmentNumber");
                Object val = m.invoke(arg);
                if (val != null) return val.toString();
            } catch (Exception ignored) {}

            try {
                // Thử lấy trường fullName (Resident/User)
                Method m = arg.getClass().getMethod("getFullName");
                Object val = m.invoke(arg);
                if (val != null) return val.toString();
            } catch (Exception ignored) {}

            try {
                // Thử lấy trường username (User)
                Method m = arg.getClass().getMethod("getUsername");
                Object val = m.invoke(arg);
                if (val != null) return val.toString();
            } catch (Exception ignored) {}
        }

        return null;
    }

    /**
     * Xây dựng mô tả chi tiết
     */
    private String buildDetails(String description, String action, String entityType, JoinPoint joinPoint, Long entityId) {
        if (description != null && !description.isEmpty()) {
            return description;
        }

        String methodName = joinPoint.getSignature().getName();
        String actionVi = switch (action) {
            case "CREATE" -> "Tạo mới";
            case "UPDATE" -> "Cập nhật";
            case "DELETE" -> "Xóa";
            case "BATCH_CREATE" -> "Tạo hàng loạt";
            default -> action;
        };
        String entityVi = switch (entityType) {
            case "INVOICE" -> "hóa đơn";
            case "CONTRACT" -> "hợp đồng";
            case "VEHICLE" -> "phương tiện";
            case "APARTMENT" -> "căn hộ";
            case "RESIDENT" -> "cư dân";
            case "USER" -> "tài khoản";
            case "PAYMENT" -> "thanh toán";
            default -> entityType;
        };

        // Xử lý batch create
        Object[] args = joinPoint.getArgs();
        if (action.equals("BATCH_CREATE") || methodName.contains("batch") || methodName.contains("Batch")) {
            return actionVi + " " + entityVi;
        }

        StringBuilder sb = new StringBuilder(actionVi + " " + entityVi);
        if (entityId != null) {
            sb.append(" #").append(entityId);
        }

        // Thêm thông tin từ request params
        for (Object arg : args) {
            if (arg == null) continue;
            try {
                Method m = arg.getClass().getMethod("getApartmentId");
                Object val = m.invoke(arg);
                if (val != null) sb.append(" (Căn hộ ID: ").append(val).append(")");
            } catch (Exception ignored) {}
        }

        return sb.toString();
    }
}
