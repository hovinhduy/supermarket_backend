package iuh.fit.supermarket.dto.sale;

import iuh.fit.supermarket.enums.InvoiceStatus;
import iuh.fit.supermarket.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO response cho trạng thái hóa đơn
 */
public record OrderStatusResponseDTO(
        Long invoiceId,
        InvoiceStatus invoiceStatus,
        PaymentMethod paymentMethod,
        BigDecimal totalAmount,
        BigDecimal amountPaid,
        String invoiceNumber,
        LocalDateTime invoiceDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
