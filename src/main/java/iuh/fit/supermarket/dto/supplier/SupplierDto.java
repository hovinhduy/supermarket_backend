package iuh.fit.supermarket.dto.supplier;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO đơn giản cho thông tin nhà cung cấp
 * Sử dụng trong các response khác cần thông tin cơ bản của nhà cung cấp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin cơ bản nhà cung cấp")
public class SupplierDto {

    /**
     * ID nhà cung cấp
     */
    @Schema(description = "ID nhà cung cấp", example = "1")
    private Integer supplierId;

    /**
     * Mã nhà cung cấp
     */
    @Schema(description = "Mã nhà cung cấp", example = "SUP0001")
    private String code;

    /**
     * Tên nhà cung cấp
     */
    @Schema(description = "Tên nhà cung cấp", example = "Công ty TNHH ABC")
    private String name;

    /**
     * Email nhà cung cấp
     */
    @Schema(description = "Email nhà cung cấp", example = "contact@abc.com")
    private String email;

    /**
     * Số điện thoại
     */
    @Schema(description = "Số điện thoại", example = "0123456789")
    private String phone;

    /**
     * Trạng thái hoạt động
     */
    @Schema(description = "Trạng thái hoạt động", example = "true")
    private Boolean isActive;
}
