package iuh.fit.supermarket.config;

import iuh.fit.supermarket.service.OrderLookupService;
import iuh.fit.supermarket.service.PromotionLookupService;
import iuh.fit.supermarket.service.ProductService;
import iuh.fit.supermarket.service.CartLookupService;
import iuh.fit.supermarket.util.SecurityUtil;
import iuh.fit.supermarket.dto.chat.tool.*;
import iuh.fit.supermarket.dto.chat.tool.ClearCartRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

/**
 * Configuration cho Spring AI Function Calling (Tools)
 *
 * Định nghĩa các Tool beans mà AI có thể gọi động dựa trên intent của user.
 * Mỗi tool được đăng ký như một Function bean với @Description để AI hiểu mục đích.
 *
 * SECURITY: Tất cả tools tự động lấy customerId từ SecurityContext
 * để đảm bảo customer chỉ có thể xem/thay đổi thông tin của chính họ.
 *
 * Lợi ích:
 * - Giảm 60-70% token cost (chỉ gọi khi cần)
 * - Tăng accuracy (AI tự quyết định tool phù hợp)
 * - Dễ mở rộng (thêm tool mới không cần sửa logic)
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class ChatToolsConfiguration {

    private final OrderLookupService orderLookupService;
    private final PromotionLookupService promotionLookupService;
    private final ProductService productService;
    private final CartLookupService cartLookupService;
    private final SecurityUtil securityUtil;

    /**
     * Tool tra cứu đơn hàng gần đây của khách hàng
     * AI sẽ gọi tool này khi user hỏi về: đơn hàng, giao hàng, mua hàng, đặt hàng
     *
     * SECURITY: customerId được lấy từ SecurityContext, không cho phép truyền vào
     */
    @Bean
    @Description("Tra cứu đơn hàng gần đây của khách hàng. Sử dụng khi user hỏi về: đơn hàng, order, giao hàng, mua hàng, đặt hàng, delivery")
    public Function<OrderLookupRequest, String> orderLookupTool() {
        return request -> {
            try {
                // SECURITY: Lấy customerId từ SecurityContext để đảm bảo chỉ xem được đơn hàng của chính mình
                Integer customerId = securityUtil.getCurrentCustomerId();

                log.info("🔧 AI Tool Called: orderLookupTool for customerId={}, limit={}",
                        customerId, request.limit());

                // Gọi service để lấy thông tin đơn hàng
                String orders = orderLookupService.getRecentOrders(
                    customerId,
                    request.limit() != null ? request.limit() : 3
                );

                log.info("✅ orderLookupTool returned {} characters", orders.length());
                return orders;
            } catch (Exception e) {
                log.error("❌ Error in orderLookupTool", e);
                return "Xin lỗi, hiện không thể tra cứu đơn hàng. Vui lòng thử lại sau.";
            }
        };
    }

    /**
     * Tool lấy thông tin khuyến mãi đang có
     * AI sẽ gọi tool này khi user hỏi về: khuyến mãi, giảm giá, sale, ưu đãi
     */
    @Bean
    @Description("Lấy thông tin khuyến mãi đang áp dụng. Sử dụng khi user hỏi về: khuyến mãi, giảm giá, sale, ưu đãi, promotion, discount")
    public Function<PromotionRequest, String> promotionTool() {
        return request -> {
            try {
                log.info("🔧 AI Tool Called: promotionTool with limit={}", request.limit());

                // Gọi service để lấy khuyến mãi
                String promotions = promotionLookupService.getActivePromotions(
                    request.limit() != null ? request.limit() : 5
                );

                log.info("✅ promotionTool returned {} characters", promotions.length());
                return promotions;
            } catch (Exception e) {
                log.error("❌ Error in promotionTool", e);
                return "Xin lỗi, hiện không thể tải thông tin khuyến mãi. Vui lòng thử lại sau.";
            }
        };
    }

    /**
     * Tool tìm kiếm sản phẩm theo tên hoặc mã
     * AI sẽ gọi tool này khi user hỏi về: sản phẩm cụ thể, tìm món, giá sản phẩm
     */
    @Bean
    @Description("Tìm kiếm sản phẩm theo tên hoặc mã. Sử dụng khi user hỏi về: sản phẩm, tìm món, giá, product, search")
    public Function<ProductSearchRequest, String> productSearchTool() {
        return request -> {
            try {
                log.info("🔧 AI Tool Called: productSearchTool with query='{}', limit={}",
                        request.query(), request.limit());

                // Tìm kiếm sản phẩm
                String searchResults = productService.searchProductsForAI(
                    request.query(),
                    request.limit() != null ? request.limit() : 5
                );

                log.info("✅ productSearchTool returned {} characters", searchResults.length());
                return searchResults;
            } catch (Exception e) {
                log.error("❌ Error in productSearchTool", e);
                return "Xin lỗi, không thể tìm kiếm sản phẩm. Vui lòng thử lại sau.";
            }
        };
    }

    /**
     * Tool kiểm tra tồn kho của sản phẩm
     * AI sẽ gọi tool này khi user hỏi về: còn hàng không, tồn kho, có sẵn không
     */
    @Bean
    @Description("Kiểm tra tình trạng tồn kho của sản phẩm. Sử dụng khi user hỏi về: còn hàng, tồn kho, stock, available")
    public Function<StockCheckRequest, String> stockCheckTool() {
        return request -> {
            try {
                log.info("🔧 AI Tool Called: stockCheckTool for productId={}", request.productId());

                // Kiểm tra tồn kho
                String stockStatus = productService.checkStockForAI(request.productId());

                log.info("✅ stockCheckTool returned: {}", stockStatus);
                return stockStatus;
            } catch (Exception e) {
                log.error("❌ Error in stockCheckTool", e);
                return "Xin lỗi, không thể kiểm tra tồn kho. Vui lòng thử lại sau.";
            }
        };
    }

    /**
     * Tool lấy thông tin chi tiết về một sản phẩm
     * AI sẽ gọi tool này khi user hỏi chi tiết về một sản phẩm cụ thể
     */
    @Bean
    @Description("Lấy thông tin chi tiết về một sản phẩm. Sử dụng khi user hỏi chi tiết về: thành phần, xuất xứ, hạn sử dụng, details")
    public Function<ProductDetailRequest, String> productDetailTool() {
        return request -> {
            try {
                log.info("🔧 AI Tool Called: productDetailTool for productId={}", request.productId());

                // Lấy chi tiết sản phẩm
                String productDetails = productService.getProductDetailsForAI(request.productId());

                log.info("✅ productDetailTool returned {} characters", productDetails.length());
                return productDetails;
            } catch (Exception e) {
                log.error("❌ Error in productDetailTool", e);
                return "Xin lỗi, không thể lấy thông tin chi tiết sản phẩm. Vui lòng thử lại sau.";
            }
        };
    }

    // ==================== CART MANAGEMENT TOOLS ====================

    /**
     * Tool thêm sản phẩm vào giỏ hàng
     * AI sẽ gọi tool này khi user muốn: thêm vào giỏ, mua, đặt mua
     *
     * SECURITY: customerId được lấy từ SecurityContext
     */
    @Bean
    @Description("Thêm sản phẩm vào giỏ hàng. Sử dụng khi user muốn: thêm vào giỏ, mua sản phẩm, cho vào giỏ, add to cart")
    public Function<AddToCartRequest, String> addToCartTool() {
        return request -> {
            try {
                // SECURITY: Lấy customerId từ SecurityContext
                Integer customerId = securityUtil.getCurrentCustomerId();

                log.info("🔧 AI Tool Called: addToCartTool - customerId={}, productUnitId={}, quantity={}",
                        customerId, request.productUnitId(), request.quantity());

                String result = cartLookupService.addToCart(
                        customerId,
                        request.productUnitId(),
                        request.productName(),
                        request.quantity()
                );

                log.info("✅ addToCartTool completed");
                return result;
            } catch (Exception e) {
                log.error("❌ Error in addToCartTool", e);
                return "Xin lỗi, không thể thêm sản phẩm vào giỏ hàng. Vui lòng thử lại sau.";
            }
        };
    }

    /**
     * Tool cập nhật số lượng sản phẩm trong giỏ hàng
     * AI sẽ gọi tool này khi user muốn: thay đổi số lượng, update, sửa số lượng
     *
     * SECURITY: customerId được lấy từ SecurityContext
     */
    @Bean
    @Description("Cập nhật số lượng sản phẩm trong giỏ hàng. Sử dụng khi user muốn: thay đổi số lượng, update số lượng, sửa số lượng")
    public Function<UpdateCartItemAIRequest, String> updateCartItemTool() {
        return request -> {
            try {
                // SECURITY: Lấy customerId từ SecurityContext
                Integer customerId = securityUtil.getCurrentCustomerId();

                log.info("🔧 AI Tool Called: updateCartItemTool - customerId={}, productUnitId={}, newQuantity={}",
                        customerId, request.productUnitId(), request.newQuantity());

                String result = cartLookupService.updateCartItem(
                        customerId,
                        request.productUnitId(),
                        request.productName(),
                        request.newQuantity()
                );

                log.info("✅ updateCartItemTool completed");
                return result;
            } catch (Exception e) {
                log.error("❌ Error in updateCartItemTool", e);
                return "Xin lỗi, không thể cập nhật giỏ hàng. Vui lòng thử lại sau.";
            }
        };
    }

    /**
     * Tool xóa sản phẩm khỏi giỏ hàng
     * AI sẽ gọi tool này khi user muốn: xóa khỏi giỏ, bỏ ra, remove
     *
     * SECURITY: customerId được lấy từ SecurityContext
     */
    @Bean
    @Description("Xóa sản phẩm khỏi giỏ hàng. Sử dụng khi user muốn: xóa khỏi giỏ, bỏ sản phẩm ra, remove from cart")
    public Function<RemoveFromCartRequest, String> removeFromCartTool() {
        return request -> {
            try {
                // SECURITY: Lấy customerId từ SecurityContext
                Integer customerId = securityUtil.getCurrentCustomerId();

                log.info("🔧 AI Tool Called: removeFromCartTool - customerId={}, productUnitId={}",
                        customerId, request.productUnitId());

                String result = cartLookupService.removeFromCart(
                        customerId,
                        request.productUnitId(),
                        request.productName()
                );

                log.info("✅ removeFromCartTool completed");
                return result;
            } catch (Exception e) {
                log.error("❌ Error in removeFromCartTool", e);
                return "Xin lỗi, không thể xóa sản phẩm khỏi giỏ hàng. Vui lòng thử lại sau.";
            }
        };
    }

    /**
     * Tool xem tổng quan giỏ hàng
     * AI sẽ gọi tool này khi user muốn: xem giỏ hàng, kiểm tra giỏ, tổng quan giỏ
     *
     * SECURITY: customerId được lấy từ SecurityContext
     */
    @Bean
    @Description("Xem tổng quan giỏ hàng. Sử dụng khi user muốn: xem giỏ hàng, kiểm tra giỏ hàng, giỏ của tôi, cart summary")
    public Function<GetCartSummaryRequest, String> getCartSummaryTool() {
        return request -> {
            try {
                // SECURITY: Lấy customerId từ SecurityContext
                Integer customerId = securityUtil.getCurrentCustomerId();

                log.info("🔧 AI Tool Called: getCartSummaryTool - customerId={}", customerId);

                String result = cartLookupService.getCartSummary(customerId);

                log.info("✅ getCartSummaryTool completed");
                return result;
            } catch (Exception e) {
                log.error("❌ Error in getCartSummaryTool", e);
                return "Xin lỗi, không thể lấy thông tin giỏ hàng. Vui lòng thử lại sau.";
            }
        };
    }

    /**
     * Tool xóa hết tất cả sản phẩm trong giỏ hàng
     * AI sẽ gọi tool này khi user muốn: xóa hết giỏ hàng, clear cart, làm mới giỏ
     *
     * SECURITY: customerId được lấy từ SecurityContext
     */
    @Bean
    @Description("Xóa hết tất cả sản phẩm trong giỏ hàng. Sử dụng khi user muốn: xóa hết giỏ, xóa tất cả, clear cart, làm mới giỏ hàng")
    public Function<ClearCartRequest, String> clearCartTool() {
        return request -> {
            try {
                // SECURITY: Lấy customerId từ SecurityContext
                Integer customerId = securityUtil.getCurrentCustomerId();

                log.info("🔧 AI Tool Called: clearCartTool - customerId={}", customerId);

                String result = cartLookupService.clearCart(customerId);

                log.info("✅ clearCartTool completed");
                return result;
            } catch (Exception e) {
                log.error("❌ Error in clearCartTool", e);
                return "Xin lỗi, không thể xóa giỏ hàng. Vui lòng thử lại sau.";
            }
        };
    }
}