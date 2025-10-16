package iuh.fit.supermarket.dto.sale;

import java.math.BigDecimal;

/**
 * DTO chứa thông tin khuyến mãi áp dụng cho chi tiết hóa đơn
 */
public record AppliedPromotionDetailDTO(
        String promotionId,
        String promotionName,
        Long promotionDetailId,
        String promotionSummary,
        String discountType,
        BigDecimal discountValue,
        Long sourceLineItemId
) {
}
