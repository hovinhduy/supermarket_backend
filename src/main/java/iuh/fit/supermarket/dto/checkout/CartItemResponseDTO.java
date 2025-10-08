package iuh.fit.supermarket.dto.checkout;

import java.math.BigDecimal;

/**
 * DTO cho sản phẩm trong giỏ hàng sau khi kiểm tra khuyến mãi
 */
public record CartItemResponseDTO(
        Long lineItemId,
        Long productUnitId,
        String unit,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        Boolean hasPromotion,
        PromotionAppliedDTO promotionApplied
) {
}
