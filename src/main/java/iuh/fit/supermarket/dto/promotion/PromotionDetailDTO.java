package iuh.fit.supermarket.dto.promotion;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho PromotionDetail
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDetailDTO {
    
    private Long detailId;
    
    @NotNull(message = "Giá trị khuyến mãi không được để trống")
    @DecimalMin(value = "0.0", message = "Giá trị khuyến mãi không được âm")
    private BigDecimal value = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Giá trị đơn hàng tối thiểu không được âm")
    private BigDecimal minOrderValue = BigDecimal.ZERO;
    
    @DecimalMin(value = "0.0", message = "Giá trị giảm giá tối đa không được âm")
    private BigDecimal maxDiscountValue;
    
    @Min(value = 1, message = "Số lượng mua tối thiểu phải lớn hơn 0")
    private Integer conditionBuyQuantity;
    
    @Min(value = 1, message = "Số lượng tặng phải lớn hơn 0")
    private Integer giftQuantity;
    
    private Long conditionProductUnitId;
    
    private Long conditionCategoryId;
    
    private Long giftProductUnitId;
    
    @NotNull(message = "ID dòng khuyến mãi không được để trống")
    private Long lineId;
    
    // Thông tin bổ sung để hiển thị
    private String conditionProductUnitName;
    private String conditionCategoryName;
    private String giftProductUnitName;
}
