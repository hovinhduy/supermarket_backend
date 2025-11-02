package iuh.fit.supermarket.dto.employee;

import iuh.fit.supermarket.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho Employee để tránh lazy loading issues
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {

    /**
     * ID duy nhất của nhân viên
     */
    private Integer employeeId;

    /**
     * Tên nhân viên
     */
    private String name;

    /**
     * Email nhân viên
     */
    private String email;

    /**
     * Mã nhân viên
     */
    private String employeeCode;

    /**
     * Vai trò của nhân viên (từ User.userRole)
     */
    private UserRole role;

    /**
     * Trạng thái xóa mềm
     */
    private Boolean isDeleted;

    /**
     * Thời gian tạo
     */
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật
     */
    private LocalDateTime updatedAt;

    /**
     * Constructor từ Employee entity
     * Sau refactoring: lấy name, email, role, isDeleted từ User entity
     */
    public static EmployeeDto fromEntity(iuh.fit.supermarket.entity.Employee employee) {
        if (employee == null) {
            return null;
        }

        // Lấy thông tin từ User entity
        var user = employee.getUser();

        return new EmployeeDto(
            employee.getEmployeeId(),
            user.getName(),           // Từ User
            user.getEmail(),          // Từ User
            employee.getEmployeeCode(),
            user.getUserRole(),       // Từ User (was employee.getRole())
            user.getIsDeleted(),      // Từ User
            user.getCreatedAt(),      // Từ User
            user.getUpdatedAt()       // Từ User
        );
    }
}
