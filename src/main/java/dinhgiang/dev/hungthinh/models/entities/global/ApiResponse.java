package dinhgiang.dev.hungthinh.models.entities.global;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lớp wrapper chuẩn cho tất cả response trả về từ API.
 * Đảm bảo mọi API đều trả về cùng format: {message, data, status}.
 *
 * @param <T> kiểu dữ liệu của trường data
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApiResponse <T> {
    /** Thông điệp mô tả kết quả */
    private String message;
    /** Dữ liệu trả về */
    private T data;
    /** Trạng thái: true = thành công, false = lỗi */
    private boolean status;

    /**
     * Tạo response thành công.
     *
     * @param data    dữ liệu trả về
     * @param message thông điệp mô tả
     * @param <T>     kiểu dữ liệu
     * @return ApiResponse với status = true
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(message, data, true);
    }

    /**
     * Tạo response lỗi.
     *
     * @param message thông điệp lỗi
     * @param <T>     kiểu dữ liệu
     * @return ApiResponse với status = false và data = null
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(message, null, false);
    }
}
