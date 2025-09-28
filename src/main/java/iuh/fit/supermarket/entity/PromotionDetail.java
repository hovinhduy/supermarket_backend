package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.enums.PromotionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity đại diện định nghĩa chi tiết khuyến mãi trong hệ thống
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
    private Long detailId;

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
     * Đơn vị sản phẩm điều kiện
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condition_product_unit_id")
    private ProductUnit conditionProductUnit;

    /**
     * Danh mục sản phẩm điều kiện
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condition_category_id")
    private Category conditionCategory;

    /**
     * Đơn vị sản phẩm tặng
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gift_product_unit_id")
    private ProductUnit giftProductUnit;

    /**
     * Liên kết đến line
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id", nullable = false)
    private PromotionLine promotionLine;

}
