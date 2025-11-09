package iuh.fit.supermarket.dto.chat.tool;

/**
 * Request DTO cho tool tra cứu đơn hàng
 * AI sẽ tự động populate các fields này dựa trên context
 *
 * Lưu ý: customerId có thể null do Spring AI không truyền được context
 * Tool sẽ tự xử lý default value
 */
public record OrderLookupRequest(
    Integer customerId,  // ID khách hàng cần tra cứu (có thể null)
    Integer limit        // Số lượng đơn hàng muốn lấy (mặc định: 3)
) {
    /**
     * Compact constructor với validation
     */
    public OrderLookupRequest {
        // Không throw exception nếu customerId null
        // Tool sẽ handle default value

        // Giới hạn tối đa 10 đơn để tránh response quá dài
        if (limit != null && (limit < 1 || limit > 10)) {
            limit = 3;
        }
    }
}