package iuh.fit.supermarket.dto.report;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO request cho báo cáo tổng kết chương trình khuyến mãi
 */
public record PromotionReportRequestDTO(
        /**
         * Ngày bắt đầu của khoảng thời gian báo cáo
         */
        @NotNull(message = "Ngày bắt đầu không được để trống")
        LocalDate fromDate,

        /**
         * Ngày kết thúc của khoảng thời gian báo cáo
         */
        @NotNull(message = "Ngày kết thúc không được để trống")
        LocalDate toDate,

        /**
         * Mã CTKM cụ thể (optional - để filter theo mã khuyến mãi cụ thể)
         */
        String promotionCode
) {
    /**
     * Compact constructor để validate dữ liệu đầu vào
     */
    public PromotionReportRequestDTO {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu không được sau ngày kết thúc");
        }
    }
}
