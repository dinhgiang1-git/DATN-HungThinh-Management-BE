package dinhgiang.dev.hungthinh.models.entities.global;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * Lớp wrapper cho response phân trang.
 * Chứa danh sách dữ liệu cùng thông tin phân trang (số trang, tổng phần tử, ...).
 *
 * @param <T> kiểu dữ liệu của từng phần tử trong danh sách
 * @author dinhgiang1
 * @version 1.0
 * @since 2026-05-10
 */
@Data
@AllArgsConstructor
public class PageResponse<T> {
    /** Danh sách dữ liệu trong trang hiện tại */
    private List<T> content;

    /** Số trang hiện tại (bắt đầu từ 1) */
    private int page;

    /** Số phần tử trong một trang */
    private int size;

    /** Tổng số phần tử */

    private long totalElements;

    /** Tổng số trang */
    private int totalPages;
}
