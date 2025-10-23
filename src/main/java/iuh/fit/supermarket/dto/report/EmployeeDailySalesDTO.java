package iuh.fit.supermarket.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO chứa thông tin doanh số của nhân viên theo ngày
 */
public record EmployeeDailySalesDTO(
        String employeeCode,
        String employeeName,
        LocalDate saleDate,
        BigDecimal totalDiscount,
        BigDecimal revenueBeforeDiscount,
        BigDecimal revenueAfterDiscount
) {}
