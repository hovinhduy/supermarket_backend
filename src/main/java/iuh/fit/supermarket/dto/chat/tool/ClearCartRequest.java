package iuh.fit.supermarket.dto.chat.tool;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO cho AI tool: clearCart
 * Xóa hết tất cả sản phẩm trong giỏ hàng
 */
public record ClearCartRequest(
        /**
         * Xác nhận xóa (optional, AI có thể bỏ qua)
         */
        @JsonProperty("confirm")
        Boolean confirm
) {
    /**
     * Constructor mặc định
     */
    public ClearCartRequest() {
        this(true);
    }
}
