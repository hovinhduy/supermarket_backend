package iuh.fit.supermarket.dto.warehouse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho thông tin tồn kho
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin tồn kho")
public class WarehouseDto {

    /**
     * ID bản ghi kho hàng
     */
    @Schema(description = "ID bản ghi kho hàng", example = "1")
    private Integer warehouseId;

    /**
     * Số lượng tồn kho hiện tại
     */
    @Schema(description = "Số lượng tồn kho hiện tại", example = "150")
    private Integer quantityOnHand;

    /**
     * Thời gian cập nhật
     */
    @Schema(description = "Thời gian cập nhật", example = "2024-01-15T10:30:00")
    private LocalDateTime updatedAt;

    /**
     * Thông tin biến thể sản phẩm
     */
    @Schema(description = "Thông tin biến thể sản phẩm")
    private ProductVariantInfo variant;

    /**
     * DTO đơn giản cho thông tin biến thể sản phẩm
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thông tin biến thể sản phẩm")
    public static class ProductVariantInfo {

        /**
         * ID biến thể
         */
        @Schema(description = "ID biến thể", example = "1")
        private Long variantId;

        /**
         * Tên biến thể
         */
        @Schema(description = "Tên biến thể", example = "Áo Thun Polo - Đỏ - L - Cái")
        private String variantName;

        /**
         * Mã biến thể (SKU)
         */
        @Schema(description = "Mã biến thể (SKU)", example = "SKU001")
        private String variantCode;

        /**
         * Mã vạch
         */
        @Schema(description = "Mã vạch", example = "1234567890123")
        private String barcode;

        /**
         * Tên sản phẩm
         */
        @Schema(description = "Tên sản phẩm", example = "Áo Thun Polo")
        private String productName;

        /**
         * Đơn vị
         */
        @Schema(description = "Đơn vị", example = "Cái")
        private String unit;
    }
}
