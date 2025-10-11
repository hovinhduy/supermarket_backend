package iuh.fit.supermarket.dto.sale;

import iuh.fit.supermarket.dto.checkout.PromotionAppliedDTO;

import java.math.BigDecimal;

/**
 * DTO phản hồi thông tin sản phẩm trong hóa đơn
 */
public record SaleItemResponseDTO(
        Integer invoiceDetailId,
        Long productUnitId,
        String productName,
        String unit,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal discountAmount,
        BigDecimal lineTotal,
        PromotionAppliedDTO promotionApplied
) {
}
