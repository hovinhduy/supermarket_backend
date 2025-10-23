package iuh.fit.supermarket.dto.report;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO request cho báo cáo doanh số bán hàng theo ngày
 */
public record SalesDailyReportRequestDTO(
        @NotNull(message = "Từ ngày không được để trống")
        LocalDate fromDate,

        @NotNull(message = "Đến ngày không được để trống")
        LocalDate toDate,

        Integer employeeId
) {
    public SalesDailyReportRequestDTO {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Từ ngày phải nhỏ hơn hoặc bằng đến ngày");
        }
    }
}
