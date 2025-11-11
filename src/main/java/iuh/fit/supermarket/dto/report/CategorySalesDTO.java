package iuh.fit.supermarket.dto.report;

import java.math.BigDecimal;

/**
 * DTO cho doanh số theo nhóm sản phẩm (Category)
 * Hiển thị doanh số trước chiết khấu, chiết khấu, và doanh số sau chiết khấu cho mỗi nhóm sản phẩm
 */
public record CategorySalesDTO(
        String categoryName,
        BigDecimal revenueBeforeDiscount,
        BigDecimal discount,
        BigDecimal revenueAfterDiscount
) {}
