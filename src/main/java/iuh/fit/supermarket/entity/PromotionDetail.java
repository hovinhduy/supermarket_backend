package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.enums.PromotionDetailType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity đại diện cho chi tiết quy tắc khuyến mãi trong hệ thống
 */
@Entity
@Table(name = "promotion_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDetail {

    /**
     * ID duy nhất của chi tiết khuyến mãi
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "detail_id")
    private Integer detailId;

    /**
     * Loại khuyến mãi
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "detail_type", nullable = false)
    private PromotionDetailType detailType;

    /**
     * Giá trị khuyến mãi
     */
    @Column(name = "value", precision = 10, scale = 2, nullable = false)
    private BigDecimal value = BigDecimal.ZERO;

    /**
     * Giá trị đơn hàng tối thiểu
     */
    @Column(name = "min_order_value", precision = 12, scale = 2)
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    /**
     * Giá trị giảm giá tối đa
     */
    @Column(name = "max_discount_value", precision = 12, scale = 2)
    private BigDecimal maxDiscountValue;

    /**
     * Số lượng mua tối thiểu (cho BUY_X_GET_Y)
     */
    @Column(name = "condition_buy_quantity")
    private Integer conditionBuyQuantity;

    /**
     * Số lượng tặng (cho BUY_X_GET_Y)
     */
    @Column(name = "gift_quantity")
    private Integer giftQuantity;

    /**
     * Chương trình khuyến mãi
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private Promotion promotion;

    /**
     * Biến thể sản phẩm điều kiện
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condition_variant_id")
    private ProductVariant conditionVariant;

    /**
     * Danh mục sản phẩm điều kiện
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condition_category_id")
    private Category conditionCategory;

    /**
     * Biến thể sản phẩm tặng
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gift_variant_id")
    private ProductVariant giftVariant;
}
