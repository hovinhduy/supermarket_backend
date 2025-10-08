package iuh.fit.supermarket.dto.checkout;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * DTO yêu cầu kiểm tra khuyến mãi cho giỏ hàng
 */
public record CheckPromotionRequestDTO(
        @NotEmpty(message = "Giỏ hàng không được trống")
        @Valid
        List<CartItemRequestDTO> items
) {
}
