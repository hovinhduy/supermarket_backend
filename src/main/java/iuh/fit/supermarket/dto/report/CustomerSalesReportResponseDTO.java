package iuh.fit.supermarket.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO response cho báo cáo doanh số theo khách hàng
 * Chứa danh sách khách hàng và tổng hợp doanh số toàn bộ
 */
public record CustomerSalesReportResponseDTO(
        LocalDate fromDate,
        LocalDate toDate,
        List<CustomerSalesSummaryDTO> customerSalesList,
        BigDecimal grandTotalDiscount,
        BigDecimal grandTotalRevenueBeforeDiscount,
        BigDecimal grandTotalRevenueAfterDiscount
) {}
