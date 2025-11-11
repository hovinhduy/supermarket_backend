package iuh.fit.supermarket.dto.report;

import java.math.BigDecimal;

/**
 * DTO projection cho kết quả query doanh số theo khách hàng và nhóm sản phẩm
 * Sử dụng để mapping kết quả từ JPQL query
 */
public record CustomerCategorySalesProjection(
        Integer customerId,
        String customerCode,
        String customerName,
        String address,
        String customerType,
        String categoryName,
        BigDecimal revenueBeforeDiscount,
        BigDecimal discount,
        BigDecimal revenueAfterDiscount
) {}
