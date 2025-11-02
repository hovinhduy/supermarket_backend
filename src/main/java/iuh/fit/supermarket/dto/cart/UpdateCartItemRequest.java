package iuh.fit.supermarket.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO request để cập nhật số lượng sản phẩm trong giỏ hàng
 */
public record UpdateCartItemRequest(
        /**
         * Số lượng sản phẩm mới
         */
        @NotNull(message = "Số lượng không được null")
        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        Integer quantity
) {
    /**
     * Constructor có validation
     */
    public UpdateCartItemRequest {
        if (quantity != null && quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
    }
}
