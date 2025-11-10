package iuh.fit.supermarket.dto.chat.tool;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO cho AI tool: addToCart
 * Thêm sản phẩm vào giỏ hàng
 */
public record AddToCartRequest(
        /**
         * Product Unit ID (lấy từ product search)
         */
        @JsonProperty("productUnitId")
        Long productUnitId,

        /**
         * Tên sản phẩm (optional, để AI dễ nhận biết)
         */
        @JsonProperty("productName")
        String productName,

        /**
         * Số lượng (mặc định 1)
         */
        @JsonProperty("quantity")
        Integer quantity
) {
    /**
     * Constructor với default quantity = 1
     */
    public AddToCartRequest(Long productUnitId, String productName) {
        this(productUnitId, productName, 1);
    }

    /**
     * Validate quantity
     */
    public Integer quantity() {
        return quantity != null && quantity > 0 ? quantity : 1;
    }
}
