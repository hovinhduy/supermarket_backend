package iuh.fit.supermarket.dto.report;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO tổng hợp doanh số của một nhân viên
 */
public record EmployeeSalesSummaryDTO(
        Integer stt,
        String employeeCode,
        String employeeName,
        List<EmployeeDailySalesDTO> dailySales,
        BigDecimal totalDiscount,
        BigDecimal totalRevenueBeforeDiscount,
        BigDecimal totalRevenueAfterDiscount
) {}
