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
     * Thông tin đơn vị sản phẩm
     */
    @Schema(description = "Thông tin đơn vị sản phẩm")
    private ProductUnitInfo productUnit;

    /**
     * DTO đơn giản cho thông tin đơn vị sản phẩm
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thông tin đơn vị sản phẩm")
    public static class ProductUnitInfo {

        /**
         * ID đơn vị sản phẩm
         */
        @Schema(description = "ID đơn vị sản phẩm", example = "1")
        private Long productUnitId;

        /**
         * Mã vạch
         */
        @Schema(description = "Mã vạch", example = "1234567890123")
        private String barcode;

        /**
         * Tỷ lệ quy đổi so với đơn vị cơ bản
         */
        @Schema(description = "Tỷ lệ quy đổi so với đơn vị cơ bản", example = "24")
        private Integer conversionValue;

        /**
         * Đánh dấu đơn vị cơ bản
         */
        @Schema(description = "Đánh dấu đơn vị cơ bản", example = "true")
        private Boolean isBaseUnit;

        /**
         * Tên sản phẩm
         */
        @Schema(description = "Tên sản phẩm", example = "Áo Thun Polo")
        private String productName;

        /**
         * Đơn vị tính
         */
        @Schema(description = "Đơn vị tính", example = "Cái")
        private String unit;
    }
}
