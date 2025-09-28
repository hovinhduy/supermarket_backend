package iuh.fit.supermarket.dto.customer;

import iuh.fit.supermarket.enums.CustomerType;
import iuh.fit.supermarket.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO cho yêu cầu tạo khách hàng mới bởi admin (không yêu cầu mật khẩu)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerRequest {

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
     * Số điện thoại khách hàng (tùy chọn cho admin creation)
     */
    @Pattern(regexp = "^[0-9+\\-\\s()]{10}$", message = "Số điện thoại không đúng định dạng")
    private String phone;

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

    /**
     * Loại khách hàng (admin có thể set REGULAR hoặc VIP)
     */
    private CustomerType customerType = CustomerType.REGULAR;
}
