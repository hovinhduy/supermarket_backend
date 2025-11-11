package iuh.fit.supermarket.dto.report;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO response cho báo cáo tổng kết chương trình khuyến mãi
 */
public record PromotionReportResponseDTO(
        /**
         * Danh sách các chương trình khuyến mãi
         */
        List<PromotionSummaryDTO> promotionList,

        /**
         * Tổng số lượng tặng (cho các CTKM loại BUY_X_GET_Y)
         */
        Integer totalGiftQuantity,

        /**
         * Tổng số tiền chiết khấu (cho các CTKM loại discount)
         */
        BigDecimal totalDiscountAmount,

        /**
         * Tổng ngân sách (tổng usageLimit của tất cả CTKM)
         */
        Integer totalBudget,

        /**
         * Tổng đã sử dụng (tổng usageCount của tất cả CTKM)
         */
        Integer totalUsed,

        /**
         * Tổng còn lại (totalBudget - totalUsed)
         */
        Integer totalRemaining
) {
}
