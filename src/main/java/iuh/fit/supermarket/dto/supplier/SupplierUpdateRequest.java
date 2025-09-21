package iuh.fit.supermarket.dto.supplier;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho yêu cầu cập nhật thông tin nhà cung cấp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật thông tin nhà cung cấp")
public class SupplierUpdateRequest {

    /**
     * Mã nhà cung cấp
     */
    @Size(max = 50, message = "Mã nhà cung cấp không được vượt quá 50 ký tự")
    @Schema(description = "Mã nhà cung cấp", example = "SUP0001")
    private String code;

    /**
     * Tên nhà cung cấp
     */
    @NotBlank(message = "Tên nhà cung cấp không được để trống")
    @Size(max = 255, message = "Tên nhà cung cấp không được vượt quá 255 ký tự")
    @Schema(description = "Tên nhà cung cấp", example = "Công ty TNHH ABC", required = true)
    private String name;

    /**
     * Địa chỉ nhà cung cấp
     */
    @Size(max = 1000, message = "Địa chỉ không được vượt quá 1000 ký tự")
    @Schema(description = "Địa chỉ nhà cung cấp", example = "123 Đường ABC, Quận 1, TP.HCM")
    private String address;

    /**
     * Email nhà cung cấp
     */
    @Email(message = "Email không hợp lệ")
    @Size(max = 255, message = "Email không được vượt quá 255 ký tự")
    @Schema(description = "Email nhà cung cấp", example = "contact@abc.com")
    private String email;

    /**
     * Số điện thoại
     */
    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    @Schema(description = "Số điện thoại", example = "0123456789")
    private String phone;

    /**
     * Trạng thái hoạt động
     */
    @Schema(description = "Trạng thái hoạt động", example = "true")
    private Boolean isActive;
}
