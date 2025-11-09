package iuh.fit.supermarket.dto.chat.structured;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Thông tin đơn hàng trong structured response
 */
public record OrderInfo(

        /**
         * ID đơn hàng
         */
        @JsonProperty(value = "order_id")
        Long orderId,

        /**
         * Mã đơn hàng
         */
        @JsonProperty(required = true, value = "order_code")
        String orderCode,

        /**
         * Trạng thái đơn hàng
         */
        @JsonProperty(required = true, value = "status")
        String status,

        /**
         * Tổng tiền
         */
        @JsonProperty(value = "total_amount")
        BigDecimal totalAmount,

        /**
         * Ngày đặt hàng
         */
        @JsonProperty(value = "order_date")
        LocalDateTime orderDate,

        /**
         * Ngày giao hàng dự kiến
         */
        @JsonProperty(value = "estimated_delivery")
        LocalDateTime estimatedDelivery,

        /**
         * Phương thức giao hàng
         */
        @JsonProperty(value = "delivery_method")
        String deliveryMethod,

        /**
         * Địa chỉ giao hàng
         */
        @JsonProperty(value = "delivery_address")
        String deliveryAddress,

        /**
         * Số lượng sản phẩm trong đơn
         */
        @JsonProperty(value = "item_count")
        Integer itemCount,

        /**
         * Danh sách sản phẩm trong đơn (tóm tắt)
         */
        @JsonProperty(value = "items")
        List<OrderItemInfo> items
) {

    /**
     * Thông tin item trong đơn hàng
     */
    public record OrderItemInfo(
            @JsonProperty(value = "product_name") String productName,
            @JsonProperty(value = "quantity") Integer quantity,
            @JsonProperty(value = "price") BigDecimal price
    ) {
    }
}
