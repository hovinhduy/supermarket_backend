package iuh.fit.supermarket.dto.report;

import iuh.fit.supermarket.enums.PromotionType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO chứa thông tin tóm tắt từng chương trình khuyến mãi trong báo cáo
 */
public record PromotionSummaryDTO(
        /**
         * Mã chương trình khuyến mãi
         */
        String promotionCode,

        /**
         * Tên chương trình khuyến mãi
         */
        String promotionName,

        /**
         * Ngày bắt đầu
         */
        LocalDate startDate,

        /**
         * Ngày kết thúc
         */
        LocalDate endDate,

        /**
         * Loại khuyến mãi
         */
        PromotionType promotionType,

        /**
         * Mã sản phẩm tặng (chỉ áp dụng cho BUY_X_GET_Y)
         */
        String giftProductCode,

        /**
         * Tên sản phẩm tặng (chỉ áp dụng cho BUY_X_GET_Y)
         */
        String giftProductName,

        /**
         * Số lượng tặng (chỉ áp dụng cho BUY_X_GET_Y)
         */
        Integer giftQuantity,

        /**
         * Đơn vị tính của sản phẩm tặng
         */
        String giftUnit,

        /**
         * Số tiền chiết khấu (áp dụng cho ORDER_DISCOUNT và PRODUCT_DISCOUNT)
         */
        BigDecimal discountAmount,

        /**
         * Giới hạn số lần sử dụng (ngân sách tổng)
         */
        Integer usageLimit,

        /**
         * Số lần đã sử dụng
         */
        Integer usageCount,

        /**
         * Số lần còn lại (usageLimit - usageCount)
         */
        Integer remainingCount
) {
}
