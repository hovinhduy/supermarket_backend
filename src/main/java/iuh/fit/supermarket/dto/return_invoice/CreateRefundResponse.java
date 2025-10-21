package iuh.fit.supermarket.dto.return_invoice;

import java.math.BigDecimal;

/**
 * DTO cho response sau khi tạo phiếu trả thành công
 */
public record CreateRefundResponse(
        Integer returnId,
        String returnCode,
        BigDecimal finalRefundAmount,
        BigDecimal reclaimedDiscountAmount,
        String status
) {
}
