package iuh.fit.supermarket.dto.supplier;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho phản hồi thông tin nhà cung cấp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin nhà cung cấp")
public class SupplierResponse {

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
     * Địa chỉ nhà cung cấp
     */
    @Schema(description = "Địa chỉ nhà cung cấp", example = "123 Đường ABC, Quận 1, TP.HCM")
    private String address;

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

    /**
     * Trạng thái xóa mềm
     */
    @Schema(description = "Trạng thái xóa mềm", example = "false")
    private Boolean isDeleted;

    /**
     * Thời gian tạo
     */
    @Schema(description = "Thời gian tạo", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật
     */
    @Schema(description = "Thời gian cập nhật", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;

    /**
     * Số lượng phiếu nhập từ nhà cung cấp này
     */
    @Schema(description = "Số lượng phiếu nhập", example = "5")
    private Integer importCount;
}
