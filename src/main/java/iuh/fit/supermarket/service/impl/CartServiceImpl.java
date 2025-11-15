package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.cart.AddCartItemRequest;
import iuh.fit.supermarket.dto.cart.CartItemResponse;
import iuh.fit.supermarket.dto.cart.CartResponse;
import iuh.fit.supermarket.dto.cart.UpdateCartItemRequest;
import iuh.fit.supermarket.dto.chat.structured.CartInfo;
import iuh.fit.supermarket.dto.checkout.CheckPromotionResponseDTO;
import iuh.fit.supermarket.dto.checkout.PromotionAppliedDTO;
import iuh.fit.supermarket.service.CartService;
import iuh.fit.supermarket.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của CartService
 * Quản lý giỏ hàng cho chatbot - Sử dụng ShoppingCartService để đảm bảo dữ liệu đồng nhất
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final ShoppingCartService shoppingCartService;

    /**
     * Thêm sản phẩm vào giỏ hàng
     * Sử dụng ShoppingCartService để đảm bảo tính khuyến mãi chính xác
     */
    @Override
    @Transactional
    public CartInfo addToCart(Integer customerId, Long productUnitId, Integer quantity) {
        log.info("Thêm sản phẩm vào giỏ hàng - Customer: {}, ProductUnit: {}, Quantity: {}",
                customerId, productUnitId, quantity);

        // Validate quantity
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }

        // Tạo request và gọi ShoppingCartService
        AddCartItemRequest request = new AddCartItemRequest(productUnitId, quantity);
        CartResponse cartResponse = shoppingCartService.addItemToCart(customerId, request);

        // Convert sang CartInfo
        return convertToCartInfo(cartResponse);
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     * Sử dụng ShoppingCartService để đảm bảo dữ liệu đồng nhất
     */
    @Override
    @Transactional
    public CartInfo removeFromCart(Integer customerId, Long productUnitId) {
        log.info("Xóa sản phẩm khỏi giỏ hàng - Customer: {}, ProductUnit: {}",
                customerId, productUnitId);

        // Gọi ShoppingCartService
        CartResponse cartResponse = shoppingCartService.removeItemFromCart(customerId, productUnitId);

        // Convert sang CartInfo
        return convertToCartInfo(cartResponse);
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     * Sử dụng ShoppingCartService để đảm bảo dữ liệu đồng nhất
     */
    @Override
    @Transactional
    public CartInfo updateQuantity(Integer customerId, Long productUnitId, Integer quantity) {
        log.info("Cập nhật số lượng sản phẩm - Customer: {}, ProductUnit: {}, NewQuantity: {}",
                customerId, productUnitId, quantity);

        // Validate quantity
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Số lượng không hợp lệ");
        }

        // Nếu quantity = 0, xóa sản phẩm
        if (quantity == 0) {
            return removeFromCart(customerId, productUnitId);
        }

        // Tạo request và gọi ShoppingCartService
        UpdateCartItemRequest request = new UpdateCartItemRequest(quantity);
        CartResponse cartResponse = shoppingCartService.updateCartItem(customerId, productUnitId, request);

        // Convert sang CartInfo
        return convertToCartInfo(cartResponse);
    }

    /**
     * Lấy thông tin giỏ hàng
     * Sử dụng ShoppingCartService để lấy dữ liệu với khuyến mãi đầy đủ
     */
    @Override
    @Transactional(readOnly = true)
    public CartInfo getCart(Integer customerId) {
        log.info("Lấy thông tin giỏ hàng - Customer: {}", customerId);

        // Gọi ShoppingCartService để lấy dữ liệu đầy đủ (bao gồm khuyến mãi)
        CartResponse cartResponse = shoppingCartService.getCart(customerId);

        // Convert sang CartInfo
        return convertToCartInfo(cartResponse);
    }

    /**
     * Xóa tất cả sản phẩm trong giỏ hàng
     * Sử dụng ShoppingCartService để đảm bảo dữ liệu đồng nhất
     */
    @Override
    @Transactional
    public CartInfo clearCart(Integer customerId) {
        log.info("Xóa tất cả sản phẩm trong giỏ hàng - Customer: {}", customerId);

        // Gọi ShoppingCartService
        shoppingCartService.clearCart(customerId);

        // Lấy lại giỏ hàng (hiện tại sẽ rỗng)
        return getCart(customerId);
    }

    // ========== Private Helper Methods ==========

    /**
     * Convert CartResponse (từ ShoppingCartService) sang CartInfo (cho chatbot)
     *
     * @param cartResponse response từ ShoppingCartService
     * @return CartInfo cho chatbot với đầy đủ thông tin khuyến mãi
     */
    private CartInfo convertToCartInfo(CartResponse cartResponse) {
        if (cartResponse == null) {
            return new CartInfo(
                    null,
                    List.of(),
                    0,
                    0.0,
                    0.0,
                    0.0,
                    0.0,
                    List.of(),
                    null
            );
        }

        // Convert items
        List<CartInfo.CartItemInfo> itemInfos = cartResponse.items().stream()
                .map(this::convertToCartItemInfo)
                .collect(Collectors.toList());

        // Convert order promotions
        List<CartInfo.OrderPromotionInfo> orderPromotions = cartResponse.appliedOrderPromotions() != null
                ? cartResponse.appliedOrderPromotions().stream()
                        .map(this::convertToOrderPromotionInfo)
                        .collect(Collectors.toList())
                : List.of();

        return new CartInfo(
                cartResponse.cartId(),
                itemInfos,
                cartResponse.totalItems(),
                cartResponse.subTotal(),
                cartResponse.lineItemDiscount(),
                cartResponse.orderDiscount(),
                cartResponse.totalPayable(),
                orderPromotions,
                cartResponse.updatedAt()
        );
    }

    /**
     * Convert CartItemResponse sang CartItemInfo
     *
     * @param itemResponse item từ CartResponse
     * @return CartItemInfo cho chatbot
     */
    private CartInfo.CartItemInfo convertToCartItemInfo(CartItemResponse itemResponse) {
        // Convert promotion applied
        CartInfo.PromotionAppliedInfo promotionInfo = null;
        if (itemResponse.promotionApplied() != null) {
            promotionInfo = convertToPromotionAppliedInfo(itemResponse.promotionApplied());
        }

        return new CartInfo.CartItemInfo(
                itemResponse.lineItemId(),
                itemResponse.productUnitId(),
                itemResponse.productName(),
                itemResponse.unitName(),
                itemResponse.quantity(),
                itemResponse.unitPrice(),
                itemResponse.originalTotal(),
                itemResponse.finalTotal(),
                itemResponse.imageUrl(),
                itemResponse.stockQuantity(),
                itemResponse.hasPromotion(),
                promotionInfo
        );
    }

    /**
     * Convert PromotionAppliedDTO sang PromotionAppliedInfo
     *
     * @param promotionDTO promotion từ CartItemResponse
     * @return PromotionAppliedInfo cho chatbot
     */
    private CartInfo.PromotionAppliedInfo convertToPromotionAppliedInfo(PromotionAppliedDTO promotionDTO) {
        if (promotionDTO == null) {
            return null;
        }

        return new CartInfo.PromotionAppliedInfo(
                promotionDTO.promotionName(),
                promotionDTO.promotionSummary(),
                promotionDTO.discountType(),
                promotionDTO.discountValue() != null ? promotionDTO.discountValue().doubleValue() : null,
                promotionDTO.sourceLineItemId()
        );
    }

    /**
     * Convert OrderPromotionDTO sang OrderPromotionInfo
     *
     * @param orderPromotion promotion từ CartResponse
     * @return OrderPromotionInfo cho chatbot
     */
    private CartInfo.OrderPromotionInfo convertToOrderPromotionInfo(
            CheckPromotionResponseDTO.OrderPromotionDTO orderPromotion) {
        if (orderPromotion == null) {
            return null;
        }

        return new CartInfo.OrderPromotionInfo(
                orderPromotion.promotionName(),
                orderPromotion.promotionSummary(),
                orderPromotion.discountValue() != null ? orderPromotion.discountValue().doubleValue() : null
        );
    }
}
