package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.enums.ApplyToType;
import iuh.fit.supermarket.enums.DiscountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity đại diện định nghĩa chi tiết khuyến mãi trong hệ thống
 * Hỗ trợ 3 loại khuyến mãi: BUY_X_GET_Y, ORDER_DISCOUNT, PRODUCT_DISCOUNT
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
     * Promotion line mà chi tiết này thuộc về
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_line_id", nullable = false)
    private PromotionLine promotionLine;

    // =====================================================
    // LOẠI 1: MUA X TẶNG Y (BUY_X_GET_Y)
    // =====================================================

    /**
     * Sản phẩm phải mua
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buy_product_id")
    private ProductUnit buyProduct;

    /**
     * Danh mục phải mua
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buy_category_id")
    private Category buyCategory;

    /**
     * Số lượng tối thiểu phải mua
     */
    @Column(name = "buy_min_quantity")
    private Integer buyMinQuantity;

    /**
     * Giá trị tối thiểu phải mua
     */
    @Column(name = "buy_min_value", precision = 18, scale = 2)
    private BigDecimal buyMinValue;

    /**
     * Sản phẩm được tặng
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gift_product_id")
    private ProductUnit giftProduct;

    /**
     * Loại giảm giá cho quà tặng (PERCENTAGE, FIXED_AMOUNT, FREE)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "gift_discount_type", length = 20)
    private DiscountType giftDiscountType;

    /**
     * Giá trị giảm giá cho quà tặng (% hoặc số tiền)
     */
    @Column(name = "gift_discount_value", precision = 18, scale = 2)
    private BigDecimal giftDiscountValue;

    /**
     * Giới hạn số lượng tặng mỗi đơn hàng
     */
    @Column(name = "gift_max_quantity")
    private Integer giftMaxQuantity;

    // =====================================================
    // LOẠI 2: GIẢM GIÁ ĐƠN HÀNG (ORDER_DISCOUNT)
    // =====================================================

    /**
     * Loại giảm giá đơn hàng (PERCENTAGE, FIXED_AMOUNT)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "order_discount_type", length = 20)
    private DiscountType orderDiscountType;

    /**
     * Giá trị giảm giá đơn hàng (% hoặc số tiền)
     */
    @Column(name = "order_discount_value", precision = 18, scale = 2)
    private BigDecimal orderDiscountValue;

    /**
     * Giới hạn giảm tối đa cho đơn hàng (nếu giảm theo %)
     */
    @Column(name = "order_discount_max_value", precision = 18, scale = 2)
    private BigDecimal orderDiscountMaxValue;

    /**
     * Tổng giá trị đơn hàng tối thiểu
     */
    @Column(name = "order_min_total_value", precision = 18, scale = 2)
    private BigDecimal orderMinTotalValue;

    /**
     * Tổng số lượng sản phẩm tối thiểu trong đơn hàng
     */
    @Column(name = "order_min_total_quantity")
    private Integer orderMinTotalQuantity;

    // =====================================================
    // LOẠI 3: GIẢM GIÁ SẢN PHẨM (PRODUCT_DISCOUNT)
    // =====================================================

    /**
     * Loại giảm giá sản phẩm (PERCENTAGE, FIXED_AMOUNT)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "product_discount_type", length = 20)
    private DiscountType productDiscountType;

    /**
     * Giá trị giảm giá sản phẩm (% hoặc số tiền)
     */
    @Column(name = "product_discount_value", precision = 18, scale = 2)
    private BigDecimal productDiscountValue;

    /**
     * Áp dụng cho loại nào (ALL, PRODUCT, CATEGORY)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "apply_to_type", length = 20)
    private ApplyToType applyToType;

    /**
     * Sản phẩm cụ thể được áp dụng
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apply_to_product_id")
    private ProductUnit applyToProduct;

    /**
     * Danh mục sản phẩm được áp dụng
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apply_to_category_id")
    private Category applyToCategory;

    /**
     * Giá trị đơn hàng tối thiểu để áp dụng giảm giá sản phẩm
     */
    @Column(name = "product_min_order_value", precision = 18, scale = 2)
    private BigDecimal productMinOrderValue;

    /**
     * Giá trị sản phẩm được khuyến mãi tối thiểu
     */
    @Column(name = "product_min_promotion_value", precision = 18, scale = 2)
    private BigDecimal productMinPromotionValue;

    /**
     * Số lượng sản phẩm được khuyến mãi tối thiểu
     */
    @Column(name = "product_min_promotion_quantity")
    private Integer productMinPromotionQuantity;

}
