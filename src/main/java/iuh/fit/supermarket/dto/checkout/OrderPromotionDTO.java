package iuh.fit.supermarket.dto.checkout;

import java.math.BigDecimal;

/**
 * DTO cho thông tin khuyến mãi áp dụng cho toàn bộ đơn hàng
 * Được lưu dưới dạng JSON trong Order entity
 *
 * @param promotionId mã khuyến mãi
 * @param promotionName tên khuyến mãi
 * @param promotionDetailId ID chi tiết khuyến mãi
 * @param promotionSummary mô tả khuyến mãi
 * @param discountType loại giảm giá (percentage, fixed_amount)
 * @param discountValue giá trị giảm giá
 */
public record OrderPromotionDTO(
    String promotionId,
    String promotionName,
    Long promotionDetailId,
    String promotionSummary,
    String discountType,
    BigDecimal discountValue
) {
    /**
     * Constructor để validate dữ liệu
     */
    public OrderPromotionDTO {
        if (promotionId == null || promotionId.isBlank()) {
            throw new IllegalArgumentException("Mã khuyến mãi không được để trống");
        }
    }
}