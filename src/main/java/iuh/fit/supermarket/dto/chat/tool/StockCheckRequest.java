package iuh.fit.supermarket.dto.chat.tool;

/**
 * Request DTO cho tool kiểm tra tồn kho
 */
public record StockCheckRequest(
    Long productId  // ID sản phẩm cần kiểm tra
) {
    /**
     * Compact constructor với validation
     */
    public StockCheckRequest {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("productId phải lớn hơn 0");
        }
    }
}