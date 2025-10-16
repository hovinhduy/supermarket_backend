package iuh.fit.supermarket.dto.sale;

import iuh.fit.supermarket.enums.InvoiceStatus;
import iuh.fit.supermarket.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO chứa thông tin hoá đơn bán cùng với các khuyến mãi được áp dụng
 */
public record SaleInvoiceFullDTO(
        Integer invoiceId,
        String invoiceNumber,
        LocalDateTime invoiceDate,
        Long orderId,
        String customerName,
        String employeeName,
        PaymentMethod paymentMethod,
        InvoiceStatus status,
        BigDecimal subtotal,
        BigDecimal totalDiscount,
        BigDecimal totalTax,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        List<SaleInvoiceItemDetailDTO> items,
        List<AppliedOrderPromotionDetailDTO> appliedOrderPromotions,
        LocalDateTime createdAt
) {
}
