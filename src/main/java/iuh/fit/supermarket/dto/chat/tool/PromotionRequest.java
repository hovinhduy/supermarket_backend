package iuh.fit.supermarket.dto.chat.tool;

/**
 * Request DTO cho tool lấy thông tin khuyến mãi
 */
public record PromotionRequest(
    Integer limit  // Số lượng khuyến mãi muốn lấy (mặc định: 5)
) {
    /**
     * Compact constructor với validation
     */
    public PromotionRequest {
        // Giới hạn tối đa 10 khuyến mãi
        if (limit != null && (limit < 1 || limit > 10)) {
            limit = 5;
        }
    }
}