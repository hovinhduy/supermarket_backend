package iuh.fit.supermarket.dto.return_invoice;

import java.math.BigDecimal;

/**
 * DTO cho thông tin giao dịch hoàn tiền
 */
public record TransactionInfo(
        Integer invoiceId,
        BigDecimal amount,
        BigDecimal maximumRefundable
) {
}
