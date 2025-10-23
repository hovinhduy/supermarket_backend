package iuh.fit.supermarket.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO response cho báo cáo doanh số bán hàng theo ngày
 */
public record SalesDailyReportResponseDTO(
        LocalDate fromDate,
        LocalDate toDate,
        List<EmployeeSalesSummaryDTO> employeeSalesList,
        BigDecimal grandTotalDiscount,
        BigDecimal grandTotalRevenueBeforeDiscount,
        BigDecimal grandTotalRevenueAfterDiscount
) {}
