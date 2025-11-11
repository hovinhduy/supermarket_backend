package iuh.fit.supermarket.dto.report;

import java.math.BigDecimal;

/**
 * DTO cho sản phẩm trong hóa đơn trả hàng
 */
public record ReturnProductItemDTO(
        String categoryName,        // Nhóm sản phẩm
        String productCode,         // Mã sản phẩm
        String productName,         // Tên sản phẩm
        String unitName,            // Đơn vị tính
        Integer quantity,           // Số lượng trả
        BigDecimal priceAtReturn,   // Đơn giá
        BigDecimal refundAmount     // Thành tiền (doanh thu hoàn trả)
) {}
