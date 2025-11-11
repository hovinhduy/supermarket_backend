package iuh.fit.supermarket.dto.chat.structured;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

/**
 * Thông tin khuyến mãi trong structured response
 * Bao gồm thông tin chung và chi tiết theo từng loại khuyến mãi
 */
public record PromotionInfo(

        /**
         * ID của promotion line (không phải header)
         */
        @JsonProperty(value = "promotion_line_id")
        Long promotionLineId,

        /**
         * Mã khuyến mãi (từ detail)
         */
        @JsonProperty(value = "promotion_code")
        String promotionCode,

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
         * Mô tả ngắn gọn, dễ hiểu cho khách hàng
         * Ví dụ: "Mua 5 tặng 1", "Giảm 10% đơn từ 500k"
         */
        @JsonProperty(value = "summary")
        String summary,

        /**
         * Loại khuyến mãi (PRODUCT_DISCOUNT, ORDER_DISCOUNT, BUY_X_GET_Y)
         */
        @JsonProperty(required = true, value = "type")
        String type,

        /**
         * Ngày bắt đầu
         */
        @JsonProperty(value = "start_date")
        LocalDate startDate,

        /**
         * Ngày kết thúc
         */
        @JsonProperty(value = "end_date")
        LocalDate endDate,

        /**
         * Trạng thái (ACTIVE, UPCOMING, EXPIRED, CANCELLED)
         */
        @JsonProperty(value = "status")
        String status,

        /**
         * Số lần sử dụng tối đa
         */
        @JsonProperty(value = "usage_limit")
        Integer usageLimit,

        /**
         * Số lần đã sử dụng
         */
        @JsonProperty(value = "usage_count")
        Integer usageCount,

        /**
         * Chi tiết cho khuyến mãi Mua X Tặng Y (nếu type = BUY_X_GET_Y)
         */
        @JsonProperty(value = "buy_x_get_y_detail")
        BuyXGetYInfo buyXGetYDetail,

        /**
         * Chi tiết cho khuyến mãi Giảm Giá Đơn Hàng (nếu type = ORDER_DISCOUNT)
         */
        @JsonProperty(value = "order_discount_detail")
        OrderDiscountInfo orderDiscountDetail,

        /**
         * Chi tiết cho khuyến mãi Giảm Giá Sản Phẩm (nếu type = PRODUCT_DISCOUNT)
         */
        @JsonProperty(value = "product_discount_detail")
        ProductDiscountInfo productDiscountDetail
) {
}
