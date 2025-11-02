package iuh.fit.supermarket.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO request để thêm sản phẩm vào giỏ hàng
 */
public record AddCartItemRequest(
        /**
         * ID của product unit cần thêm
         */
        @NotNull(message = "Product unit ID không được null")
        Long productUnitId,

        /**
         * Số lượng sản phẩm
         */
        @NotNull(message = "Số lượng không được null")
        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        Integer quantity
) {
    /**
     * Constructor có validation
     */
    public AddCartItemRequest {
        if (productUnitId != null && productUnitId <= 0) {
            throw new IllegalArgumentException("Product unit ID phải lớn hơn 0");
        }
        if (quantity != null && quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
    }
}
