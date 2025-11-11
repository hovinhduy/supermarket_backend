package iuh.fit.supermarket.dto.return_invoice;

import jakarta.validation.constraints.NotNull;

/**
 * DTO cho request tạo phiếu trả hàng toàn bộ
 */
public record CreateRefundRequest(
        @NotNull(message = "ID hóa đơn không được để trống")
        Integer invoiceId,

        String reasonNote
) {
}
