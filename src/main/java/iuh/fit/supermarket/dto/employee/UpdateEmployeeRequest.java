package iuh.fit.supermarket.dto.employee;

import iuh.fit.supermarket.enums.Gender;
import iuh.fit.supermarket.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO cho request cập nhật thông tin nhân viên
 * Tất cả các trường đều optional, chỉ cập nhật những trường được gửi lên
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmployeeRequest {

    /**
     * Tên đầy đủ của nhân viên
     */
    @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
    private String name;

    /**
     * Email nhân viên
     */
    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    /**
     * Số điện thoại
     */
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phone;

    /**
     * Vai trò của nhân viên (ADMIN, MANAGER, hoặc STAFF)
     */
    private UserRole role;

    /**
     * Ngày sinh
     */
    private LocalDate dateOfBirth;

    /**
     * Giới tính
     */
    private Gender gender;

    /**
     * Mã nhân viên
     */
    @Size(max = 50, message = "Mã nhân viên không được vượt quá 50 ký tự")
    private String employeeCode;
}
