package iuh.fit.supermarket.dto.chat.structured;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Thông tin chi tiết cho khuyến mãi Mua X Tặng Y
 */
public record BuyXGetYInfo(

        /**
         * Tên sản phẩm phải mua
         */
        @JsonProperty(value = "buy_product_name")
        String buyProductName,

        /**
         * Số lượng tối thiểu phải mua
         */
        @JsonProperty(value = "buy_min_quantity")
        Integer buyMinQuantity,

        /**
         * Giá trị tối thiểu phải mua
         */
        @JsonProperty(value = "buy_min_value")
        BigDecimal buyMinValue,

        /**
         * Tên sản phẩm được tặng/giảm giá
         */
        @JsonProperty(value = "gift_product_name")
        String giftProductName,

        /**
         * Số lượng sản phẩm tặng/giảm
         */
        @JsonProperty(value = "gift_quantity")
        Integer giftQuantity,

        /**
         * Loại giảm giá (FREE, PERCENTAGE, FIXED_AMOUNT)
         */
        @JsonProperty(value = "gift_discount_type")
        String giftDiscountType,

        /**
         * Giá trị giảm (% hoặc số tiền)
         */
        @JsonProperty(value = "gift_discount_value")
        BigDecimal giftDiscountValue,

        /**
         * Số lần áp dụng tối đa
         */
        @JsonProperty(value = "gift_max_quantity")
        Integer giftMaxQuantity
) {
}
