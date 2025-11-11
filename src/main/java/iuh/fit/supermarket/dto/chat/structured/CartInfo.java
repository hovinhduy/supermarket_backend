package iuh.fit.supermarket.dto.chat.structured;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO thông tin giỏ hàng cho AI structured response
 * Parse từ CartResponse để trả về cho frontend
 */
public record CartInfo(

        /**
         * ID giỏ hàng
         */
        @JsonProperty(value = "cart_id")
        Integer cartId,

        /**
         * Danh sách items trong giỏ
         */
        @JsonProperty(value = "items")
        List<CartItemInfo> items,

        /**
         * Tổng số lượng items (không tính quà tặng)
         */
        @JsonProperty(value = "total_items")
        Integer totalItems,

        /**
         * Tổng tiền trước khuyến mãi
         */
        @JsonProperty(value = "sub_total")
        Double subTotal,

        /**
         * Giảm giá từ sản phẩm
         */
        @JsonProperty(value = "line_item_discount")
        Double lineItemDiscount,

        /**
         * Giảm giá từ đơn hàng
         */
        @JsonProperty(value = "order_discount")
        Double orderDiscount,

        /**
         * Tổng tiền cần thanh toán
         */
        @JsonProperty(value = "total_payable")
        Double totalPayable,

        /**
         * Thời gian cập nhật
         */
        @JsonProperty(value = "updated_at")
        LocalDateTime updatedAt
) {

    /**
     * DTO cho từng item trong giỏ
     */
    public record CartItemInfo(
            /**
             * ID của product unit (QUAN TRỌNG: để frontend thao tác)
             */
            @JsonProperty(value = "product_unit_id")
            Long productUnitId,

            /**
             * Tên sản phẩm
             */
            @JsonProperty(value = "product_name")
            String productName,

            /**
             * Đơn vị
             */
            @JsonProperty(value = "unit_name")
            String unitName,

            /**
             * Số lượng
             */
            @JsonProperty(value = "quantity")
            Integer quantity,

            /**
             * Giá đơn vị
             */
            @JsonProperty(value = "unit_price")
            Double unitPrice,

            /**
             * Tổng giá trước khuyến mãi
             */
            @JsonProperty(value = "original_total")
            Double originalTotal,

            /**
             * Tổng giá sau khuyến mãi
             */
            @JsonProperty(value = "final_total")
            Double finalTotal,

            /**
             * URL hình ảnh
             */
            @JsonProperty(value = "image_url")
            String imageUrl,

            /**
             * Số lượng tồn kho
             */
            @JsonProperty(value = "stock_quantity")
            Integer stockQuantity,

            /**
             * Có khuyến mãi không
             */
            @JsonProperty(value = "has_promotion")
            Boolean hasPromotion,

            /**
             * Tên khuyến mãi (nếu có)
             */
            @JsonProperty(value = "promotion_name")
            String promotionName
    ) {
    }
}
