package iuh.fit.supermarket.dto.promotion;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho request tính toán discount sản phẩm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalculateProductDiscountRequest {
    
    @NotNull(message = "ID product unit không được để trống")
    private Long productUnitId;
    
    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer quantity;
    
    @NotNull(message = "Đơn giá không được để trống")
    @DecimalMin(value = "0.0", message = "Đơn giá không được âm")
    private BigDecimal unitPrice;
}
