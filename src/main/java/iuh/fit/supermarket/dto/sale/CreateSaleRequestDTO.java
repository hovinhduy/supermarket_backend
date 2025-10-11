package iuh.fit.supermarket.dto.sale;

import iuh.fit.supermarket.enums.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO yêu cầu tạo bán hàng
 */
public record CreateSaleRequestDTO(
        @NotNull(message = "ID nhân viên không được null")
        Integer employeeId,

        Integer customerId,

        @NotNull(message = "Phương thức thanh toán không được null")
        PaymentMethod paymentMethod,

        @NotNull(message = "Số tiền khách trả không được null")
        @DecimalMin(value = "0.0", message = "Số tiền khách trả phải >= 0")
        BigDecimal amountPaid,

        String note,

        @NotEmpty(message = "Danh sách sản phẩm không được rỗng")
        @Valid
        List<SaleItemRequestDTO> items,

        @Valid
        List<OrderPromotionRequestDTO> appliedOrderPromotions
) {
}
