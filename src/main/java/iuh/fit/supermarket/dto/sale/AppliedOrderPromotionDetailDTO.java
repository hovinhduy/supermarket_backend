package iuh.fit.supermarket.dto.sale;

import java.math.BigDecimal;

/**
 * DTO chứa thông tin khuyến mãi áp dụng cho toàn đơn hàng
 */
public record AppliedOrderPromotionDetailDTO(
        String promotionId,
        String promotionName,
        Long promotionDetailId,
        String promotionSummary,
        String discountType,
        BigDecimal discountValue
) {
}
