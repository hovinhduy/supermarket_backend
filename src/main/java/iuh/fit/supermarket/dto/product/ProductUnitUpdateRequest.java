package iuh.fit.supermarket.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

/**
 * DTO cho yêu cầu cập nhật đơn vị sản phẩm
 */
@Schema(description = "Yêu cầu cập nhật đơn vị sản phẩm")
public record ProductUnitUpdateRequest(

        /**
         * Tên đơn vị tính (sẽ tự động tạo unit nếu chưa tồn tại)
         */
        @Schema(description = "Tên đơn vị tính", example = "Lon") String unitName,

        /**
         * Tỷ lệ quy đổi so với đơn vị cơ bản
         */
        @Positive(message = "Tỷ lệ quy đổi phải là số dương") @Schema(description = "Tỷ lệ quy đổi so với đơn vị cơ bản", example = "1") Integer conversionValue,

        /**
         * Có phải là đơn vị cơ bản không
         */
        @Schema(description = "Có phải là đơn vị cơ bản không", example = "true") Boolean isBaseUnit,

        /**
         * Mã vạch (tùy chọn)
         */
        @Schema(description = "Mã vạch của đơn vị sản phẩm", example = "1234567890123") String barcode,

        /**
         * Trạng thái hoạt động
         */
        @Schema(description = "Trạng thái hoạt động", example = "true") Boolean isActive) {

    /**
     * Compact canonical constructor với validation
     */
    public ProductUnitUpdateRequest {
        // Trim và validate unitName nếu có
        if (unitName != null) {
            unitName = unitName.trim();
            if (unitName.isEmpty()) {
                unitName = null;
            }
        }

        // Validate conversionValue nếu có
        if (conversionValue != null && conversionValue <= 0) {
            throw new IllegalArgumentException("Tỷ lệ quy đổi phải là số dương");
        }

        // Tự động điều chỉnh conversion value = 1 cho đơn vị cơ bản
        if (isBaseUnit != null && isBaseUnit && conversionValue != null && conversionValue != 1) {
            conversionValue = 1;
        }

        // Trim barcode nếu có
        if (barcode != null) {
            barcode = barcode.trim();
            if (barcode.isEmpty()) {
                barcode = null;
            }
        }
    }

    /**
     * Kiểm tra có dữ liệu cập nhật không
     *
     * @return true nếu có ít nhất một trường được cập nhật
     */
    public boolean hasUpdates() {
        return unitName != null || conversionValue != null ||
                isBaseUnit != null || barcode != null || isActive != null;
    }
}
