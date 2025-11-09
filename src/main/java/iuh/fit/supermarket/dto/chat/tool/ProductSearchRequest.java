package iuh.fit.supermarket.dto.chat.tool;

/**
 * Request DTO cho tool tìm kiếm sản phẩm
 */
public record ProductSearchRequest(
    String query,   // Từ khóa tìm kiếm (tên sản phẩm hoặc mã)
    Integer limit   // Số lượng kết quả trả về (mặc định: 5)
) {
    /**
     * Compact constructor với validation
     */
    public ProductSearchRequest {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query không được trống");
        }
        // Giới hạn tối đa 10 sản phẩm
        if (limit != null && (limit < 1 || limit > 10)) {
            limit = 5;
        }
    }
}