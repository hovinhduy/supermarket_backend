package iuh.fit.supermarket.dto.employee;

import iuh.fit.supermarket.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO request cho việc tìm kiếm nhân viên với nhiều tiêu chí
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSearchRequest {

    /**
     * Từ khóa tìm kiếm (tìm trong tên, mã nhân viên, email)
     */
    private String keyword;

    /**
     * Lọc theo role cụ thể
     */
    private UserRole role;

    /**
     * Số trang (bắt đầu từ 0)
     */
    private Integer page = 0;

    /**
     * Số lượng record trên mỗi trang
     */
    private Integer size = 10;

    /**
     * Trường sắp xếp (name, email, employeeCode, createdAt)
     */
    private String sortBy = "createdAt";

    /**
     * Hướng sắp xếp (ASC, DESC)
     */
    private String sortDirection = "DESC";
}
