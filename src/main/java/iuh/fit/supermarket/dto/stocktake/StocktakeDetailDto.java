package iuh.fit.supermarket.dto.stocktake;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho thông tin chi tiết kiểm kê kho
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin chi tiết kiểm kê kho")
public class StocktakeDetailDto {

    /**
     * ID chi tiết kiểm kê
     */
    @Schema(description = "ID chi tiết kiểm kê", example = "1")
    private Integer stocktakeDetailId;

    /**
     * Số lượng tồn kho theo hệ thống tại thời điểm tạo phiếu
     */
    @Schema(description = "Số lượng tồn kho theo hệ thống", example = "100")
    private Integer quantityExpected;

    /**
     * Số lượng thực tế đếm được
     */
    @Schema(description = "Số lượng thực tế đếm được", example = "95")
    private Integer quantityCounted;

    /**
     * Chênh lệch (counted - expected)
     */
    @Schema(description = "Chênh lệch (counted - expected)", example = "-5")
    private Integer quantityDifference;

    /**
     * Số lượng tăng
     */
    @Schema(description = "Số lượng tăng", example = "0")
    private Integer quantityIncrease;

    /**
     * Số lượng giảm
     */
    @Schema(description = "Số lượng giảm", example = "5")
    private Integer quantityDecrease;

    /**
     * Ghi chú lý do chênh lệch
     */
    @Schema(description = "Ghi chú lý do chênh lệch", example = "Hàng bị hỏng do vận chuyển")
    private String reason;

    /**
     * Thời gian tạo
     */
    @Schema(description = "Thời gian tạo", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

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
