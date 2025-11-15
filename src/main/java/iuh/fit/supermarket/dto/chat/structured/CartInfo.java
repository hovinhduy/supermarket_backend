package iuh.fit.supermarket.dto.chat.structured;

import com.fasterxml.jackson.annotation.JsonProperty;
import iuh.fit.supermarket.dto.checkout.CheckPromotionResponseDTO;

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
         * Danh sách items trong giỏ (bao gồm cả quà tặng từ khuyến mãi)
         */
        @JsonProperty(value = "items")
        List<CartItemInfo> items,

        /**
         * Tổng số lượng items (chỉ tính sản phẩm mua, không tính quà tặng)
         */
        @JsonProperty(value = "total_items")
        Integer totalItems,

        /**
         * Tổng tiền trước khuyến mãi
         */
        @JsonProperty(value = "sub_total")
        Double subTotal,

        /**
         * Giảm giá từ sản phẩm (PRODUCT_DISCOUNT + BUY_X_GET_Y)
         */
        @JsonProperty(value = "line_item_discount")
        Double lineItemDiscount,

        /**
         * Giảm giá từ đơn hàng (ORDER_DISCOUNT)
         */
        @JsonProperty(value = "order_discount")
        Double orderDiscount,

        /**
         * Tổng tiền cần thanh toán
         */
        @JsonProperty(value = "total_payable")
        Double totalPayable,

        /**
         * Danh sách khuyến mãi đơn hàng đã áp dụng
         */
        @JsonProperty(value = "applied_order_promotions")
        List<OrderPromotionInfo> appliedOrderPromotions,

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
             * ID dòng item (để reference từ quà tặng)
             */
            @JsonProperty(value = "line_item_id")
            Long lineItemId,

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
             * Thông tin khuyến mãi được áp dụng
             */
            @JsonProperty(value = "promotion_applied")
            PromotionAppliedInfo promotionApplied
    ) {
    }

    /**
     * DTO thông tin khuyến mãi được áp dụng cho từng item
     */
    public record PromotionAppliedInfo(
            @JsonProperty(value = "promotion_name")
            String promotionName,

            @JsonProperty(value = "promotion_summary")
            String promotionSummary,

            @JsonProperty(value = "discount_type")
            String discountType,

            @JsonProperty(value = "discount_value")
            Double discountValue,

            /**
             * ID của line item gốc (cho quà tặng BUY_X_GET_Y)
             */
            @JsonProperty(value = "source_line_item_id")
            Long sourceLineItemId
    ) {
    }

    /**
     * DTO thông tin khuyến mãi đơn hàng
     */
    public record OrderPromotionInfo(
            @JsonProperty(value = "promotion_name")
            String promotionName,

            @JsonProperty(value = "promotion_summary")
            String promotionSummary,

            @JsonProperty(value = "discount_value")
            Double discountValue
    ) {
    }
}
