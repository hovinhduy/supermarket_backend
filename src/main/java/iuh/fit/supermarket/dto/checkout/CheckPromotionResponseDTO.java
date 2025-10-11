package iuh.fit.supermarket.dto.checkout;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO phản hồi sau khi kiểm tra và áp dụng khuyến mãi
 */
public record CheckPromotionResponseDTO(
        List<CartItemResponseDTO> items,
        SummaryDTO summary,
        List<OrderPromotionDTO> appliedOrderPromotions
) {
    public record SummaryDTO(
            BigDecimal subTotal,
            BigDecimal orderDiscount,
            BigDecimal lineItemDiscount,
            BigDecimal totalPayable
    ) {
    }

    /**
     * DTO cho thông tin khuyến mãi đơn hàng (ORDER_DISCOUNT)
     */
    public record OrderPromotionDTO(
            String promotionId,
            String promotionName,
            Long promotionDetailId,
            String promotionSummary,
            String discountType,
            BigDecimal discountValue
    ) {
    }
}
