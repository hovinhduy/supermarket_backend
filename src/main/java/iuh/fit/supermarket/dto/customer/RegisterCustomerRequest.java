package iuh.fit.supermarket.dto.customer;

import iuh.fit.supermarket.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO cho yêu cầu đăng ký khách hàng (self-registration)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterCustomerRequest {

    /**
     * Tên khách hàng
     */
    @NotBlank(message = "Tên khách hàng không được để trống")
    @Size(min = 2, max = 100, message = "Tên khách hàng phải từ 2 đến 100 ký tự")
    private String name;

    /**
     * Email khách hàng
     */
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Size(max = 100, message = "Email không được vượt quá 100 ký tự")
    private String email;

    /**
     * Số điện thoại khách hàng (bắt buộc cho registration)
     */
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9+\\-\\s()]{10,20}$", message = "Số điện thoại không đúng định dạng")
    private String phone;

    /**
     * Mật khẩu (bắt buộc cho registration)
     */
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 50, message = "Mật khẩu phải từ 6 đến 50 ký tự")
    private String password;

    /**
     * Xác nhận mật khẩu
     */
    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;

    /**
     * Giới tính
     */
    private Gender gender;

    /**
     * Địa chỉ khách hàng
     */
    @Size(max = 255, message = "Địa chỉ không được vượt quá 255 ký tự")
    private String address;

    /**
     * Ngày sinh khách hàng
     */
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate dateOfBirth;
}
