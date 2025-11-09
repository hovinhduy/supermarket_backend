package iuh.fit.supermarket.dto.chat.structured;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Thông tin khuyến mãi trong structured response
 */
public record PromotionInfo(

        /**
         * ID khuyến mãi
         */
        @JsonProperty(value = "promotion_id")
        Long promotionId,

        /**
         * Tên chương trình khuyến mãi
         */
        @JsonProperty(required = true, value = "name")
        String name,

        /**
         * Mô tả khuyến mãi
         */
        @JsonProperty(value = "description")
        String description,

        /**
         * Loại khuyến mãi (PERCENTAGE, FIXED_AMOUNT, BUY_X_GET_Y)
         */
        @JsonProperty(value = "type")
        String type,

        /**
         * Giá trị giảm (% hoặc số tiền)
         */
        @JsonProperty(value = "discount_value")
        BigDecimal discountValue,

        /**
         * Ngày bắt đầu
         */
        @JsonProperty(value = "start_date")
        LocalDateTime startDate,

        /**
         * Ngày kết thúc
         */
        @JsonProperty(value = "end_date")
        LocalDateTime endDate,

        /**
         * Điều kiện áp dụng
         */
        @JsonProperty(value = "conditions")
        String conditions,

        /**
         * Áp dụng cho sản phẩm/danh mục nào
         */
        @JsonProperty(value = "applicable_to")
        String applicableTo,

        /**
         * Còn hiệu lực không?
         */
        @JsonProperty(value = "is_active")
        Boolean isActive
) {
}
