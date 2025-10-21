package iuh.fit.supermarket.dto.return_invoice;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho response tính toán preview trả hàng
 */
public record RefundCalculationResponse(
        BigDecimal maximumRefundable,
        List<RefundLineItemResponse> refundLineItems,
        TransactionInfo transaction
) {
}
