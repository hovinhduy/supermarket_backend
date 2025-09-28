package iuh.fit.supermarket.dto.promotion;

import iuh.fit.supermarket.service.PromotionService.OrderItemDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho request tính toán discount
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculateDiscountRequest {
    
    @NotNull(message = "Giá trị đơn hàng không được để trống")
    @DecimalMin(value = "0.0", message = "Giá trị đơn hàng không được âm")
    private BigDecimal orderAmount;
    
    @Valid
    private List<OrderItemDTO> orderItems;
}
