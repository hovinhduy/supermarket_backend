package iuh.fit.supermarket.dto.report;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO tóm tắt doanh số của một khách hàng
 * Bao gồm thông tin khách hàng và chi tiết doanh số theo từng nhóm sản phẩm
 */
public record CustomerSalesSummaryDTO(
        Integer customerId,
        String customerCode,
        String customerName,
        String address,
        String customerType,
        List<CategorySalesDTO> categorySalesList,
        BigDecimal totalDiscount,
        BigDecimal totalRevenueBeforeDiscount,
        BigDecimal totalRevenueAfterDiscount
) {}
