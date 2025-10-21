package iuh.fit.supermarket.dto.return_invoice;

import java.math.BigDecimal;

/**
 * DTO cho response của mỗi dòng sản phẩm trả (có tính toán)
 */
public record RefundLineItemResponse(
        Integer lineItemId,
        Integer quantity,
        BigDecimal price,
        BigDecimal subtotal,
        BigDecimal originalPrice,
        BigDecimal discountedPrice,
        BigDecimal discountedSubtotal,
        Integer maximumRefundableQuantity,
        BigDecimal totalCartDiscountAmount
) {
}
