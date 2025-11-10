package iuh.fit.supermarket.dto.chat.tool;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO cho AI tool: updateCartItem
 * Cập nhật số lượng sản phẩm trong giỏ hàng
 */
public record UpdateCartItemAIRequest(
        /**
         * Product Unit ID
         */
        @JsonProperty("productUnitId")
        Long productUnitId,

        /**
         * Tên sản phẩm (optional, để AI dễ nhận biết)
         */
        @JsonProperty("productName")
        String productName,

        /**
         * Số lượng mới
         */
        @JsonProperty("newQuantity")
        Integer newQuantity
) {
    /**
     * Validate newQuantity phải > 0
     */
    public boolean isValid() {
        return productUnitId != null && newQuantity != null && newQuantity > 0;
    }
}
