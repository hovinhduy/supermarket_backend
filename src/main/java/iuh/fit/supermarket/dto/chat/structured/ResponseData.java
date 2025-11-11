package iuh.fit.supermarket.dto.chat.structured;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO chứa dữ liệu có cấu trúc của response
 *
 * Chỉ một trong các field sẽ có giá trị, tùy thuộc vào responseType
 */
public record ResponseData(

        /**
         * Danh sách sản phẩm (khi responseType = PRODUCT_INFO)
         */
        @JsonProperty(value = "products")
        List<ProductInfo> products,

        /**
         * Danh sách đơn hàng (khi responseType = ORDER_INFO)
         */
        @JsonProperty(value = "orders")
        List<OrderInfo> orders,

        /**
         * Danh sách khuyến mãi (khi responseType = PROMOTION_INFO)
         */
        @JsonProperty(value = "promotions")
        List<PromotionInfo> promotions,

        /**
         * Thông tin tồn kho (khi responseType = STOCK_INFO)
         */
        @JsonProperty(value = "stock")
        StockInfo stock,

        /**
         * Thông tin chính sách/chung (khi responseType = GENERAL_ANSWER)
         */
        @JsonProperty(value = "policy")
        PolicyInfo policy,

        /**
         * Thông tin giỏ hàng (khi responseType = CART_INFO)
         */
        @JsonProperty(value = "cart")
        CartInfo cart
) {

    /**
     * Factory method để tạo ResponseData chỉ chứa products
     */
    public static ResponseData withProducts(List<ProductInfo> products) {
        return new ResponseData(products, null, null, null, null, null);
    }

    /**
     * Factory method để tạo ResponseData chỉ chứa orders
     */
    public static ResponseData withOrders(List<OrderInfo> orders) {
        return new ResponseData(null, orders, null, null, null, null);
    }

    /**
     * Factory method để tạo ResponseData chỉ chứa promotions
     */
    public static ResponseData withPromotions(List<PromotionInfo> promotions) {
        return new ResponseData(null, null, promotions, null, null, null);
    }

    /**
     * Factory method để tạo ResponseData chỉ chứa stock
     */
    public static ResponseData withStock(StockInfo stock) {
        return new ResponseData(null, null, null, stock, null, null);
    }

    /**
     * Factory method để tạo ResponseData chỉ chứa policy
     */
    public static ResponseData withPolicy(PolicyInfo policy) {
        return new ResponseData(null, null, null, null, policy, null);
    }

    /**
     * Factory method để tạo ResponseData chỉ chứa cart
     */
    public static ResponseData withCart(CartInfo cart) {
        return new ResponseData(null, null, null, null, null, cart);
    }
}
