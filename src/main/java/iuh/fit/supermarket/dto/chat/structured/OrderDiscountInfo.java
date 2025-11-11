package iuh.fit.supermarket.dto.chat.structured;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Thông tin chi tiết cho khuyến mãi Giảm Giá Đơn Hàng
 */
public record OrderDiscountInfo(

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
         * Giảm tối đa (nếu giảm theo %)
         */
        @JsonProperty(value = "max_discount")
        BigDecimal maxDiscount,

        /**
         * Giá trị đơn hàng tối thiểu
         */
        @JsonProperty(value = "min_order_value")
        BigDecimal minOrderValue,

        /**
         * Số lượng sản phẩm tối thiểu
         */
        @JsonProperty(value = "min_order_quantity")
        Integer minOrderQuantity
) {
}
