package iuh.fit.supermarket.dto.address;

import iuh.fit.supermarket.enums.AddressLabel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO yêu cầu cập nhật địa chỉ giao hàng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAddressRequest {

    /**
     * Tên người nhận hàng
     */
    @NotBlank(message = "Tên người nhận không được để trống")
    @Size(min = 2, max = 100, message = "Tên người nhận phải từ 2 đến 100 ký tự")
    private String recipientName;

    /**
     * Số điện thoại người nhận
     */
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0|\\+84)[0-9]{9,10}$", message = "Số điện thoại không đúng định dạng")
    private String recipientPhone;

    /**
     * Địa chỉ chi tiết (số nhà, tên đường)
     */
    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    @Size(max = 255, message = "Địa chỉ chi tiết không được vượt quá 255 ký tự")
    private String addressLine;

    /**
     * Phường/Xã
     */
    @Size(max = 100, message = "Phường/Xã không được vượt quá 100 ký tự")
    private String ward;

    /**
     * Tỉnh/Thành phố
     */
    @Size(max = 100, message = "Tỉnh/Thành phố không được vượt quá 100 ký tự")
    private String city;

    /**
     * Đánh dấu địa chỉ mặc định
     */
    private Boolean isDefault;

    /**
     * Nhãn địa chỉ (Nhà, Văn phòng, Tòa nhà)
     */
    private AddressLabel label;
}
