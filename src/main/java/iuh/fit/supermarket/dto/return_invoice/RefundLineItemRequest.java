package iuh.fit.supermarket.dto.return_invoice;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO cho mỗi dòng sản phẩm được trả
 */
public record RefundLineItemRequest(
        @NotNull(message = "ID dòng hóa đơn không được để trống")
        Integer lineItemId,

        @NotNull(message = "Số lượng trả không được để trống")
        @Min(value = 1, message = "Số lượng trả phải lớn hơn 0")
        Integer quantity
) {
}
