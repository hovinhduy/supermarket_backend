package iuh.fit.supermarket.dto.supplier;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho yêu cầu cập nhật trạng thái hoạt động của nhà cung cấp
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật trạng thái hoạt động nhà cung cấp")
public class SupplierStatusUpdateRequest {

    /**
     * Trạng thái hoạt động của nhà cung cấp
     */
    @NotNull(message = "Trạng thái hoạt động không được để trống")
    @Schema(description = "Trạng thái hoạt động", example = "true", required = true)
    private Boolean isActive;
}
