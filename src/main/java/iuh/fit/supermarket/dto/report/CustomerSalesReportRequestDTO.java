package iuh.fit.supermarket.dto.report;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO request cho báo cáo doanh số theo khách hàng
 */
public record CustomerSalesReportRequestDTO(
        @NotNull(message = "Từ ngày không được để trống")
        LocalDate fromDate,

        @NotNull(message = "Đến ngày không được để trống")
        LocalDate toDate,

        Integer customerId
) {
    /**
     * Compact constructor để validate dữ liệu đầu vào
     */
    public CustomerSalesReportRequestDTO {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Từ ngày phải nhỏ hơn hoặc bằng đến ngày");
        }
    }
}
