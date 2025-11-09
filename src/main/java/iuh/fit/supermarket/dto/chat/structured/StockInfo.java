package iuh.fit.supermarket.dto.chat.structured;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Thông tin tồn kho trong structured response
 */
public record StockInfo(

        /**
         * ID sản phẩm
         */
        @JsonProperty(value = "product_id")
        Long productId,

        /**
         * Tên sản phẩm
         */
        @JsonProperty(required = true, value = "product_name")
        String productName,

        /**
         * Mã sản phẩm
         */
        @JsonProperty(value = "product_code")
        String productCode,

        /**
         * Số lượng tồn kho
         */
        @JsonProperty(value = "quantity")
        Integer quantity,

        /**
         * Trạng thái: IN_STOCK, LOW_STOCK, OUT_OF_STOCK
         */
        @JsonProperty(required = true, value = "status")
        String status,

        /**
         * Đơn vị tính
         */
        @JsonProperty(value = "unit")
        String unit,

        /**
         * Vị trí kho
         */
        @JsonProperty(value = "warehouse_location")
        String warehouseLocation,

        /**
         * Thông tin bổ sung
         */
        @JsonProperty(value = "note")
        String note
) {

    /**
     * Enum định nghĩa trạng thái tồn kho
     */
    public enum StockStatus {
        IN_STOCK,      // Còn hàng
        LOW_STOCK,     // Sắp hết hàng
        OUT_OF_STOCK   // Hết hàng
    }
}
