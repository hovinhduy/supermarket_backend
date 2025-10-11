package iuh.fit.supermarket.dto.sale;

import iuh.fit.supermarket.enums.OrderStatus;
import iuh.fit.supermarket.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO response cho trạng thái đơn hàng
 */
public record OrderStatusResponseDTO(
        Long orderId,
        OrderStatus status,
        PaymentMethod paymentMethod,
        BigDecimal totalAmount,
        BigDecimal amountPaid,
        String invoiceNumber,
        LocalDateTime invoiceDate,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
