package iuh.fit.supermarket.dto.checkout;

import java.math.BigDecimal;

/**
 * DTO chứa thông tin khuyến mãi được áp dụng
 */
public record PromotionAppliedDTO(
        String promotionId,
        String promotionName,
        Long promotionDetailId,
        String promotionSummary,
        String discountType,
        BigDecimal discountValue,
        Long sourceLineItemId
) {
}
