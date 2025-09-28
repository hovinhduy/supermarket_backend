package iuh.fit.supermarket.dto.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho yêu cầu đổi mật khẩu khách hàng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    /**
     * Mật khẩu cũ
     */
    @NotBlank(message = "Mật khẩu cũ không được để trống")
    private String oldPassword;

    /**
     * Mật khẩu mới
     */
    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 6, max = 50, message = "Mật khẩu mới phải từ 6 đến 50 ký tự")
    private String newPassword;

    /**
     * Xác nhận mật khẩu mới
     */
    @NotBlank(message = "Xác nhận mật khẩu không được để trống")
    private String confirmPassword;
}
