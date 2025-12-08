package iuh.fit.supermarket.dto.employee;

import iuh.fit.supermarket.enums.Gender;
import iuh.fit.supermarket.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO cho request tạo nhân viên mới
 * Chỉ chứa các trường cần thiết để tạo Employee + User
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeRequest {

    /**
     * Tên đầy đủ của nhân viên
     */
    @NotBlank(message = "Tên không được để trống")
    @Size(max = 100, message = "Tên không được vượt quá 100 ký tự")
    private String name;

    /**
     * Email nhân viên (phải duy nhất)
     */
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    /**
     * Số điện thoại (tùy chọn)
     */
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    private String phone;

    /**
     * Mật khẩu (chưa hash, sẽ được mã hóa ở service)
     */
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;

    /**
     * Vai trò của nhân viên (ADMIN, MANAGER, hoặc STAFF)
     */
    @NotNull(message = "Vai trò không được để trống")
    private UserRole role;

    /**
     * Ngày sinh (tùy chọn)
     */
    private LocalDate dateOfBirth;

    /**
     * Giới tính (tùy chọn)
     */
    private Gender gender;

    /**
     * Mã nhân viên (tùy chọn, nếu không nhập sẽ tự sinh)
     */
    @Size(max = 50, message = "Mã nhân viên không được vượt quá 50 ký tự")
    private String employeeCode;
}
