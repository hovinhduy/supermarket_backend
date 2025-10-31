package iuh.fit.supermarket.config;

import iuh.fit.supermarket.service.OrderLookupService;
import iuh.fit.supermarket.service.PromotionLookupService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

/**
 * Configuration cho AI Function Calling
 * Định nghĩa các functions mà AI có thể tự động gọi
 */
@Configuration
public class ChatFunctionConfig {

    /**
     * Function cho AI tra cứu khuyến mãi
     */
    @Bean
    @Description("Lấy danh sách chương trình khuyến mãi đang có của siêu thị")
    public Function<PromotionRequest, String> getActivePromotions(PromotionLookupService promotionService) {
        return request -> {
            System.out.println(">>> AI đang gọi function: getActivePromotions");
            int limit = request.limit() != null ? request.limit() : 5;
            return promotionService.getActivePromotions(limit);
        };
    }

    /**
     * Function cho AI tìm kiếm khuyến mãi theo từ khóa
     */
    @Bean
    @Description("Tìm kiếm chương trình khuyến mãi theo từ khóa")
    public Function<SearchPromotionRequest, String> searchPromotions(PromotionLookupService promotionService) {
        return request -> {
            System.out.println(">>> AI đang gọi function: searchPromotions với keyword: " + request.keyword());
            return promotionService.searchPromotions(request.keyword());
        };
    }

    /**
     * Function cho AI tra cứu đơn hàng
     */
    @Bean
    @Description("Lấy danh sách đơn hàng gần đây của khách hàng")
    public Function<OrderRequest, String> getRecentOrders(OrderLookupService orderService) {
        return request -> {
            System.out.println(">>> AI đang gọi function: getRecentOrders cho customer: " + request.customerId());
            int limit = request.limit() != null ? request.limit() : 5;
            return orderService.getRecentOrders(request.customerId(), limit);
        };
    }

    /**
     * Function cho AI tra cứu chi tiết đơn hàng
     */
    @Bean
    @Description("Lấy chi tiết của một đơn hàng cụ thể")
    public Function<OrderDetailRequest, String> getOrderDetails(OrderLookupService orderService) {
        return request -> {
            System.out.println(">>> AI đang gọi function: getOrderDetails cho order: " + request.orderId());
            return orderService.getOrderDetails(request.orderId(), request.customerId());
        };
    }

    /**
     * Request để lấy khuyến mãi
     */
    public record PromotionRequest(Integer limit) {}

    /**
     * Request để tìm kiếm khuyến mãi
     */
    public record SearchPromotionRequest(String keyword) {}

    /**
     * Request để lấy đơn hàng
     */
    public record OrderRequest(Integer customerId, Integer limit) {}

    /**
     * Request để lấy chi tiết đơn hàng
     */
    public record OrderDetailRequest(Long orderId, Integer customerId) {}
}
