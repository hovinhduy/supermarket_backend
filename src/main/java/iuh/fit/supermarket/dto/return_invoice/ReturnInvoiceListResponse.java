package iuh.fit.supermarket.dto.return_invoice;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO cho danh sách hóa đơn trả hàng
 */
public record ReturnInvoiceListResponse(
        Integer returnId,
        String returnCode,
        LocalDateTime returnDate,
        String invoiceNumber,
        String customerName,
        String customerPhone,
        String employeeName,
        BigDecimal totalRefundAmount,
        BigDecimal reclaimedDiscountAmount,
        BigDecimal finalRefundAmount,
        String reasonNote
) {
}
