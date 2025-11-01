package iuh.fit.supermarket.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho yêu cầu đăng nhập của khách hàng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerLoginRequest {

    /**
     * Email hoặc số điện thoại đăng nhập
     */
    @NotBlank(message = "Email hoặc số điện thoại không được để trống")
    private String emailOrPhone;

    /**
     * Mật khẩu đăng nhập
     */
    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự")
    private String password;
}
