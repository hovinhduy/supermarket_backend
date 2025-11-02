package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.cart.AddCartItemRequest;
import iuh.fit.supermarket.dto.cart.CartResponse;
import iuh.fit.supermarket.dto.cart.UpdateCartItemRequest;

/**
 * Service interface để quản lý giỏ hàng
 */
public interface ShoppingCartService {

    /**
     * Lấy giỏ hàng của khách hàng
     *
     * @param customerId ID khách hàng
     * @return Thông tin giỏ hàng
     */
    CartResponse getCart(Integer customerId);

    /**
     * Thêm sản phẩm vào giỏ hàng
     *
     * @param customerId ID khách hàng
     * @param request    Thông tin sản phẩm cần thêm
     * @return Thông tin giỏ hàng sau khi thêm
     */
    CartResponse addItemToCart(Integer customerId, AddCartItemRequest request);

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     *
     * @param customerId    ID khách hàng
     * @param productUnitId ID product unit
     * @param request       Thông tin cập nhật
     * @return Thông tin giỏ hàng sau khi cập nhật
     */
    CartResponse updateCartItem(Integer customerId, Long productUnitId, UpdateCartItemRequest request);

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     *
     * @param customerId    ID khách hàng
     * @param productUnitId ID product unit
     * @return Thông tin giỏ hàng sau khi xóa
     */
    CartResponse removeItemFromCart(Integer customerId, Long productUnitId);

    /**
     * Xóa toàn bộ giỏ hàng
     *
     * @param customerId ID khách hàng
     */
    void clearCart(Integer customerId);
}
