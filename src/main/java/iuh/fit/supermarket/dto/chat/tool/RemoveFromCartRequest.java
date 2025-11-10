package iuh.fit.supermarket.dto.chat.tool;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO cho AI tool: removeFromCart
 * Xóa sản phẩm khỏi giỏ hàng
 */
public record RemoveFromCartRequest(
        /**
         * Product Unit ID
         */
        @JsonProperty("productUnitId")
        Long productUnitId,

        /**
         * Tên sản phẩm (optional, để AI dễ nhận biết)
         */
        @JsonProperty("productName")
        String productName
) {
}
