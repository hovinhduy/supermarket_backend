package iuh.fit.supermarket.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * DTO cho yêu cầu tạo đơn vị sản phẩm
 */
@Schema(description = "Yêu cầu tạo đơn vị sản phẩm")
public record ProductUnitRequest(

        /**
         * Tên đơn vị tính (sẽ tự động tạo unit nếu chưa tồn tại)
         */
        @NotBlank(message = "Tên đơn vị tính không được để trống") @Schema(description = "Tên đơn vị tính", example = "Kilogram", required = true) String unitName,

        /**
         * Tỷ lệ quy đổi so với đơn vị cơ bản
         */
        @NotNull(message = "Tỷ lệ quy đổi không được để trống") @Positive(message = "Tỷ lệ quy đổi phải là số dương") @Schema(description = "Tỷ lệ quy đổi so với đơn vị cơ bản", example = "1", required = true) Integer conversionValue,

        /**
         * Có phải là đơn vị cơ bản không
         */
        @NotNull(message = "Cờ đơn vị cơ bản không được để trống") @Schema(description = "Có phải là đơn vị cơ bản không", example = "true", required = true) Boolean isBaseUnit,

        /**
         * Mã đơn vị sản phẩm tùy chỉnh (tùy chọn, sẽ tự động tạo nếu null hoặc rỗng)
         */
        @Schema(description = "Mã đơn vị sản phẩm tùy chỉnh", example = "COCA_CHAI_001") String code,

        /**
         * Mã vạch (tùy chọn)
         */
        @Schema(description = "Mã vạch của đơn vị sản phẩm", example = "1234567890123") String barcode) {
    /**
     * Compact canonical constructor với validation
     */
    public ProductUnitRequest {
        if (unitName == null || unitName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đơn vị tính không được để trống");
        }
        // Trim và validate unitName
        unitName = unitName.trim();
        if (conversionValue == null) {
            throw new IllegalArgumentException("Tỷ lệ quy đổi không được để trống");
        }
        if (conversionValue <= 0) {
            throw new IllegalArgumentException("Tỷ lệ quy đổi phải là số dương");
        }
        if (isBaseUnit == null) {
            throw new IllegalArgumentException("Cờ đơn vị cơ bản không được để trống");
        }
        // Tự động điều chỉnh conversion value = 1 cho đơn vị cơ bản
        if (isBaseUnit && conversionValue != 1) {
            conversionValue = 1;
        }
        // Trim code nếu có
        if (code != null) {
            code = code.trim();
            if (code.isEmpty()) {
                code = null;
            }
        }

        // Trim barcode nếu có
        if (barcode != null) {
            barcode = barcode.trim();
            if (barcode.isEmpty()) {
                barcode = null;
            }
        }
    }
}
