package iuh.fit.supermarket.dto.employee;

import iuh.fit.supermarket.enums.Gender;
import iuh.fit.supermarket.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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
     * Số điện thoại nhân viên
     */
    private String phone;

    /**
     * Mã nhân viên
     */
    private String employeeCode;

    /**
     * Vai trò của nhân viên (từ User.userRole)
     */
    private UserRole role;

    /**
     * Ngày sinh nhân viên
     */
    private LocalDate dateOfBirth;

    /**
     * Giới tính nhân viên
     */
    private Gender gender;

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
     * Lấy thông tin từ User entity liên kết
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
            user.getPhone(),          // Từ User
            employee.getEmployeeCode(),
            user.getUserRole(),       // Từ User
            user.getDateOfBirth(),    // Từ User
            user.getGender(),         // Từ User
            user.getIsDeleted(),      // Từ User
            user.getCreatedAt(),      // Từ User
            user.getUpdatedAt()       // Từ User
        );
    }
}
