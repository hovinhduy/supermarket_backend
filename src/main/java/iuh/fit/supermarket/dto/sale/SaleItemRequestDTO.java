package iuh.fit.supermarket.dto.sale;

import iuh.fit.supermarket.dto.checkout.PromotionAppliedDTO;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO cho từng sản phẩm trong yêu cầu bán hàng
 */
public record SaleItemRequestDTO(
        @NotNull(message = "ID đơn vị sản phẩm không được null")
        Long productUnitId,

        @NotNull(message = "Số lượng không được null")
        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        Integer quantity,

        @NotNull(message = "Đơn giá không được null")
        @DecimalMin(value = "0.0", inclusive = false, message = "Đơn giá phải lớn hơn 0")
        BigDecimal unitPrice,

        @NotNull(message = "Tổng tiền dòng không được null")
        @DecimalMin(value = "0.0", message = "Tổng tiền dòng phải >= 0")
        BigDecimal lineTotal,

        PromotionAppliedDTO promotionApplied
) {
}
