package iuh.fit.supermarket.dto.employee;

import iuh.fit.supermarket.enums.EmployeeRole;
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
     * Vai trò của nhân viên
     */
    private EmployeeRole role;

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
     */
    public static EmployeeDto fromEntity(iuh.fit.supermarket.entity.Employee employee) {
        if (employee == null) {
            return null;
        }
        
        return new EmployeeDto(
            employee.getEmployeeId(),
            employee.getName(),
            employee.getEmail(),
            employee.getEmployeeCode(),
            employee.getRole(),
            employee.getIsDeleted(),
            employee.getCreatedAt(),
            employee.getUpdatedAt()
        );
    }
}
