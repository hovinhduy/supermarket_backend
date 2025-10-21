package iuh.fit.supermarket.dto.return_invoice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * DTO cho request tạo phiếu trả hàng
 */
public record CreateRefundRequest(
        @NotNull(message = "ID hóa đơn không được để trống")
        Integer invoiceId,

        @NotEmpty(message = "Danh sách sản phẩm trả không được rỗng")
        @Valid
        List<RefundLineItemRequest> refundLineItems,

        String reasonNote
) {
}
