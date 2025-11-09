package iuh.fit.supermarket.dto.chat.tool;

/**
 * Request DTO cho tool lấy chi tiết sản phẩm
 */
public record ProductDetailRequest(
    Long productId  // ID sản phẩm cần lấy chi tiết
) {
    /**
     * Compact constructor với validation
     */
    public ProductDetailRequest {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("productId phải lớn hơn 0");
        }
    }
}