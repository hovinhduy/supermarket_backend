package iuh.fit.supermarket.dto.chat.structured;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Thông tin chi tiết cho khuyến mãi Giảm Giá Sản Phẩm
 */
public record ProductDiscountInfo(

        /**
         * Loại giảm giá (PERCENTAGE, FIXED_AMOUNT)
         */
        @JsonProperty(value = "discount_type")
        String discountType,

        /**
         * Giá trị giảm (% hoặc số tiền)
         */
        @JsonProperty(value = "discount_value")
        BigDecimal discountValue,

        /**
         * Áp dụng cho (ALL, PRODUCT)
         */
        @JsonProperty(value = "apply_to_type")
        String applyToType,

        /**
         * Tên sản phẩm áp dụng (nếu có)
         */
        @JsonProperty(value = "apply_to_product_name")
        String applyToProductName,

        /**
         * Giá trị đơn hàng tối thiểu
         */
        @JsonProperty(value = "min_order_value")
        BigDecimal minOrderValue,

        /**
         * Giá trị sản phẩm tối thiểu
         */
        @JsonProperty(value = "min_promotion_value")
        BigDecimal minPromotionValue,

        /**
         * Số lượng sản phẩm tối thiểu
         */
        @JsonProperty(value = "min_promotion_quantity")
        Integer minPromotionQuantity
) {
}
