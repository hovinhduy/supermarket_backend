package iuh.fit.supermarket.dto.sale;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO phản hồi sau khi tạo bán hàng thành công
 */
public record CreateSaleResponseDTO(
        Integer invoiceId,
        String invoiceNumber,
        LocalDateTime invoiceDate,
        BigDecimal subtotal,
        BigDecimal totalDiscount,
        BigDecimal totalAmount,
        BigDecimal amountPaid,
        BigDecimal changeAmount,
        String customerName,
        String employeeName,
        List<SaleItemResponseDTO> items,
        Long orderCode,
        String paymentUrl,
        String qrCode,
        String status
) {
}
