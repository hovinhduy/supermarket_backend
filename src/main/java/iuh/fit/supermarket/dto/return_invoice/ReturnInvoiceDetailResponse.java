package iuh.fit.supermarket.dto.return_invoice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho chi tiết đầy đủ của phiếu trả hàng
 */
public record ReturnInvoiceDetailResponse(
        Integer returnId,
        String returnCode,
        LocalDateTime returnDate,
        String invoiceNumber,
        Integer invoiceId,
        CustomerInfo customer,
        EmployeeInfo employee,
        BigDecimal totalRefundAmount,
        BigDecimal reclaimedDiscountAmount,
        BigDecimal finalRefundAmount,
        String reasonNote,
        List<ReturnItemDetail> returnDetails,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record CustomerInfo(
            Integer customerId,
            String name,
            String phone,
            String email
    ) {
    }

    public record EmployeeInfo(
            Integer employeeId,
            String name,
            String email
    ) {
    }

    public record ReturnItemDetail(
            Integer returnDetailId,
            Integer quantity,
            String productName,
            String productUnit,
            BigDecimal priceAtReturn,
            BigDecimal refundAmount
    ) {
    }
}
