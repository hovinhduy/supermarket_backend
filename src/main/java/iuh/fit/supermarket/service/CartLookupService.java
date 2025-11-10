package iuh.fit.supermarket.service;

/**
 * Service interface cho AI cart operations
 * Wraps ShoppingCartService và format output cho AI
 */
public interface CartLookupService {

    /**
     * Thêm sản phẩm vào giỏ hàng
     *
     * @param customerId Customer ID
     * @param productUnitId Product Unit ID
     * @param productName Tên sản phẩm (optional)
     * @param quantity Số lượng
     * @return Kết quả dạng text cho AI
     */
    String addToCart(Integer customerId, Long productUnitId, String productName, Integer quantity);

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     *
     * @param customerId Customer ID
     * @param productUnitId Product Unit ID
     * @param productName Tên sản phẩm (optional)
     * @param newQuantity Số lượng mới
     * @return Kết quả dạng text cho AI
     */
    String updateCartItem(Integer customerId, Long productUnitId, String productName, Integer newQuantity);

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     *
     * @param customerId Customer ID
     * @param productUnitId Product Unit ID
     * @param productName Tên sản phẩm (optional)
     * @return Kết quả dạng text cho AI
     */
    String removeFromCart(Integer customerId, Long productUnitId, String productName);

    /**
     * Lấy tổng quan giỏ hàng
     *
     * @param customerId Customer ID
     * @return Giỏ hàng dạng text cho AI
     */
    String getCartSummary(Integer customerId);
}
