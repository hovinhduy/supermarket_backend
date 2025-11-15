package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.chat.structured.CartInfo;

/**
 * Service để quản lý giỏ hàng cho chatbot
 */
public interface CartService {
    
    /**
     * Thêm sản phẩm vào giỏ hàng
     * Nếu sản phẩm đã có trong giỏ, tăng số lượng
     * 
     * @param customerId ID khách hàng
     * @param productUnitId ID đơn vị sản phẩm
     * @param quantity số lượng thêm vào
     * @return thông tin giỏ hàng sau khi thêm
     */
    CartInfo addToCart(Integer customerId, Long productUnitId, Integer quantity);
    
    /**
     * Xóa sản phẩm khỏi giỏ hàng
     * 
     * @param customerId ID khách hàng
     * @param productUnitId ID đơn vị sản phẩm
     * @return thông tin giỏ hàng sau khi xóa
     */
    CartInfo removeFromCart(Integer customerId, Long productUnitId);
    
    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     * Nếu quantity = 0, xóa sản phẩm khỏi giỏ
     * 
     * @param customerId ID khách hàng
     * @param productUnitId ID đơn vị sản phẩm
     * @param quantity số lượng mới
     * @return thông tin giỏ hàng sau khi cập nhật
     */
    CartInfo updateQuantity(Integer customerId, Long productUnitId, Integer quantity);
    
    /**
     * Lấy thông tin giỏ hàng của khách hàng
     * 
     * @param customerId ID khách hàng
     * @return thông tin giỏ hàng
     */
    CartInfo getCart(Integer customerId);
    
    /**
     * Xóa tất cả sản phẩm trong giỏ hàng
     * 
     * @param customerId ID khách hàng
     * @return thông tin giỏ hàng rỗng
     */
    CartInfo clearCart(Integer customerId);
}
