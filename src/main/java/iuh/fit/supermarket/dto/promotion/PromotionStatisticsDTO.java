package iuh.fit.supermarket.dto.promotion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho thống kê promotion
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionStatisticsDTO {
    
    private long totalPromotions;
    private long activePromotions;
    private long pausedPromotions;
    private long upcomingPromotions;
    private long expiredPromotions;
    
    private long totalPromotionLines;
    private long activePromotionLines;
    
    private long totalPromotionDetails;
    
    private long promotionsExpiringThisWeek;
    private long promotionsExpiringThisMonth;
    
    private BigDecimal totalDiscountValue;
    private BigDecimal averageDiscountPerPromotion;
    
    private long buyXGetYPromotions;
    private long percentDiscountPromotions;
    private long fixedDiscountPromotions;
}
