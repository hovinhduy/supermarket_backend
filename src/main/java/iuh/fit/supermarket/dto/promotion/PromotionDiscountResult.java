package iuh.fit.supermarket.dto.promotion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho kết quả tính toán discount từ promotion
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDiscountResult {
    
    private BigDecimal totalDiscount = BigDecimal.ZERO;
    private BigDecimal finalAmount = BigDecimal.ZERO;
    private List<AppliedPromotionDTO> appliedPromotions;
    private List<GiftItemDTO> giftItems;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AppliedPromotionDTO {
        private Long promotionLineId;
        private String promotionLineName;
        private String promotionType;
        private BigDecimal discountAmount;
        private String description;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GiftItemDTO {
        private Long productUnitId;
        private String productUnitName;
        private Integer quantity;
        private String promotionLineName;
    }
}
