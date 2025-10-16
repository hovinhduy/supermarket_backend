package iuh.fit.supermarket.dto.sale;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO chứa chi tiết sản phẩm trong hóa đơn bán cùng với các khuyến mãi được áp dụng
 */
public record SaleInvoiceItemDetailDTO(
        Integer invoiceDetailId,
        Long productUnitId,
        String productName,
        String unit,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal discountAmount,
        BigDecimal lineTotal,
        List<AppliedPromotionDetailDTO> appliedPromotions
) {
}
