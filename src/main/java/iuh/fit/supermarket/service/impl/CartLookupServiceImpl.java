package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.cart.AddCartItemRequest;
import iuh.fit.supermarket.dto.cart.CartItemResponse;
import iuh.fit.supermarket.dto.cart.CartResponse;
import iuh.fit.supermarket.dto.cart.UpdateCartItemRequest;
import iuh.fit.supermarket.service.CartLookupService;
import iuh.fit.supermarket.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementation của CartLookupService
 * Xử lý cart operations cho AI chatbot
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartLookupServiceImpl implements CartLookupService {

    private final ShoppingCartService shoppingCartService;

    /**
     * Thêm sản phẩm vào giỏ hàng
     */
    @Override
    public String addToCart(Integer customerId, Long productUnitId, String productName, Integer quantity) {
        log.info("🛒 AI Tool: addToCart - customerId={}, productUnitId={}, quantity={}",
                customerId, productUnitId, quantity);

        try {
            // Validate input
            if (productUnitId == null || productUnitId <= 0) {
                return "❌ Lỗi: Product ID không hợp lệ. Vui lòng tìm kiếm sản phẩm trước khi thêm vào giỏ.";
            }

            if (quantity == null || quantity <= 0) {
                quantity = 1;
            }

            // Gọi service để thêm vào giỏ
            AddCartItemRequest request = new AddCartItemRequest(productUnitId, quantity);
            CartResponse cart = shoppingCartService.addItemToCart(customerId, request);

            // Format response cho AI
            return formatCartResponse(cart, "Đã thêm " + (productName != null ? productName : "sản phẩm") + " vào giỏ hàng");

        } catch (Exception e) {
            log.error("Lỗi khi thêm sản phẩm vào giỏ hàng", e);
            return "❌ Xin lỗi, không thể thêm sản phẩm vào giỏ hàng. Lỗi: " + e.getMessage();
        }
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     */
    @Override
    public String updateCartItem(Integer customerId, Long productUnitId, String productName, Integer newQuantity) {
        log.info("🛒 AI Tool: updateCartItem - customerId={}, productUnitId={}, newQuantity={}",
                customerId, productUnitId, newQuantity);

        try {
            // Validate input
            if (productUnitId == null || productUnitId <= 0) {
                return "❌ Lỗi: Product ID không hợp lệ.";
            }

            if (newQuantity == null || newQuantity <= 0) {
                return "❌ Lỗi: Số lượng mới phải lớn hơn 0. Nếu muốn xóa sản phẩm, vui lòng dùng chức năng 'Xóa khỏi giỏ hàng'.";
            }

            // Gọi service để update
            UpdateCartItemRequest request = new UpdateCartItemRequest(newQuantity);
            CartResponse cart = shoppingCartService.updateCartItem(customerId, productUnitId, request);

            // Format response cho AI
            return formatCartResponse(cart, "Đã cập nhật số lượng " + (productName != null ? productName : "sản phẩm") + " thành " + newQuantity);

        } catch (Exception e) {
            log.error("Lỗi khi cập nhật giỏ hàng", e);
            return "❌ Xin lỗi, không thể cập nhật giỏ hàng. Lỗi: " + e.getMessage();
        }
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     */
    @Override
    public String removeFromCart(Integer customerId, Long productUnitId, String productName) {
        log.info("🛒 AI Tool: removeFromCart - customerId={}, productUnitId={}",
                customerId, productUnitId);

        try {
            // Validate input
            if (productUnitId == null || productUnitId <= 0) {
                return "❌ Lỗi: Product ID không hợp lệ.";
            }

            // Gọi service để xóa
            CartResponse cart = shoppingCartService.removeItemFromCart(customerId, productUnitId);

            // Format response cho AI
            return formatCartResponse(cart, "Đã xóa " + (productName != null ? productName : "sản phẩm") + " khỏi giỏ hàng");

        } catch (Exception e) {
            log.error("Lỗi khi xóa sản phẩm khỏi giỏ hàng", e);
            return "❌ Xin lỗi, không thể xóa sản phẩm. Lỗi: " + e.getMessage();
        }
    }

    /**
     * Lấy tổng quan giỏ hàng
     */
    @Override
    public String getCartSummary(Integer customerId) {
        log.info("🛒 AI Tool: getCartSummary - customerId={}", customerId);

        try {
            // Lấy giỏ hàng
            CartResponse cart = shoppingCartService.getCart(customerId);

            // Format response cho AI
            if (cart.items() == null || cart.items().isEmpty()) {
                return "🛒 Giỏ hàng của bạn đang trống.\n\n💡 Gợi ý: Tìm kiếm sản phẩm và thêm vào giỏ hàng để mua sắm!";
            }

            return formatCartSummary(cart);

        } catch (Exception e) {
            log.error("Lỗi khi lấy giỏ hàng", e);
            return "❌ Xin lỗi, không thể lấy thông tin giỏ hàng. Lỗi: " + e.getMessage();
        }
    }

    /**
     * Format cart response cho AI (sau khi thêm/update/xóa)
     */
    private String formatCartResponse(CartResponse cart, String action) {
        StringBuilder result = new StringBuilder();
        result.append("✅ ").append(action).append("!\n\n");
        result.append(formatCartSummary(cart));
        return result.toString();
    }

    /**
     * Format cart summary cho AI
     */
    private String formatCartSummary(CartResponse cart) {
        StringBuilder result = new StringBuilder();

        result.append("🛒 GIỎ HÀNG CỦA BẠN\n");
        result.append("━━━━━━━━━━━━━━━━━━━━\n\n");

        int itemCount = 0;
        for (CartItemResponse item : cart.items()) {
            itemCount++;
            // Combine product name and unit name
            String displayName = item.productName() + " (" + item.unitName() + ")";

            result.append(String.format("%d. %s x%d\n",
                    itemCount,
                    displayName,
                    item.quantity()));

            result.append(String.format("   💰 Giá: %,.0fđ x %d = %,.0fđ\n",
                    item.unitPrice(),
                    item.quantity(),
                    item.finalTotal()));

            // Show promotion if has
            if (item.hasPromotion() != null && item.hasPromotion()) {
                result.append(String.format("   🎁 Đã áp dụng khuyến mãi (Gốc: %,.0fđ)\n",
                        item.originalTotal()));
            }

            result.append("\n");
        }

        result.append("━━━━━━━━━━━━━━━━━━━━\n");
        result.append(String.format("📦 Tổng số lượng: %d sản phẩm\n", cart.totalItems()));
        result.append(String.format("💵 Tổng tiền: %,.0fđ\n", cart.subTotal()));

        // Thông tin khuyến mãi (nếu có)
        double totalDiscount = (cart.lineItemDiscount() != null ? cart.lineItemDiscount() : 0)
                + (cart.orderDiscount() != null ? cart.orderDiscount() : 0);

        if (totalDiscount > 0) {
            result.append(String.format("🎁 Tổng giảm giá: -%,.0fđ\n", totalDiscount));
            result.append(String.format("💰 Thành tiền: %,.0fđ\n", cart.totalPayable()));
        }

        // Thông tin miễn phí ship
        double payableAmount = cart.totalPayable() != null ? cart.totalPayable() : cart.subTotal();
        if (payableAmount >= 200000) {
            result.append("\n🚚 MIỄN PHÍ GIAO HÀNG!");
        } else {
            double remaining = 200000 - payableAmount;
            result.append(String.format("\n📍 Mua thêm %,.0fđ để được MIỄN PHÍ SHIP!", remaining));
        }

        return result.toString();
    }
}
