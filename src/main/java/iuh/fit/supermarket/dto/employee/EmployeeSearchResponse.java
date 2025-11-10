package iuh.fit.supermarket.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO response cho kết quả tìm kiếm nhân viên với thông tin phân trang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSearchResponse {

    /**
     * Danh sách nhân viên tìm được
     */
    private List<EmployeeDto> employees;

    /**
     * Tổng số nhân viên tìm được (không phân trang)
     */
    private Long totalElements;

    /**
     * Tổng số trang
     */
    private Integer totalPages;

    /**
     * Trang hiện tại (bắt đầu từ 0)
     */
    private Integer currentPage;

    /**
     * Số lượng record trên mỗi trang
     */
    private Integer pageSize;

    /**
     * Có trang tiếp theo hay không
     */
    private Boolean hasNext;

    /**
     * Có trang trước hay không
     */
    private Boolean hasPrevious;
}
