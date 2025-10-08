package iuh.fit.supermarket.dto.checkout;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO cho sản phẩm trong giỏ hàng khi kiểm tra khuyến mãi
 */
public record CartItemRequestDTO(
        @NotNull(message = "Product unit ID không được để trống")
        Long productUnitId,

        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        Integer quantity
) {
}
