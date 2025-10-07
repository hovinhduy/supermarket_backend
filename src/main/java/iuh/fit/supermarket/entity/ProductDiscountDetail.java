package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.enums.ApplyToType;
import iuh.fit.supermarket.enums.DiscountType;
import iuh.fit.supermarket.enums.PromotionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity cho khuyến mãi loại Giảm Giá Sản Phẩm
 * Áp dụng giảm giá cho sản phẩm cụ thể, danh mục hoặc tất cả sản phẩm
 */
@Entity
@DiscriminatorValue("PRODUCT_DISCOUNT")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ProductDiscountDetail extends PromotionDetail {

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

    @Override
    public PromotionType getPromotionType() {
        return PromotionType.PRODUCT_DISCOUNT;
    }

    @Override
    public void validate() {
        if (productDiscountType == null) {
            throw new IllegalArgumentException("Loại giảm giá sản phẩm không được để trống");
        }
        if (productDiscountValue == null || productDiscountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá trị giảm giá sản phẩm phải lớn hơn 0");
        }
        if (productDiscountType == DiscountType.PERCENTAGE && 
            (productDiscountValue.compareTo(BigDecimal.ZERO) <= 0 || 
             productDiscountValue.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new IllegalArgumentException("Giá trị giảm giá phần trăm phải từ 1-100");
        }
        if (applyToType == null) {
            throw new IllegalArgumentException("Loại áp dụng không được để trống");
        }
        if (applyToType == ApplyToType.PRODUCT && applyToProduct == null) {
            throw new IllegalArgumentException("Phải chỉ định sản phẩm khi áp dụng cho sản phẩm cụ thể");
        }
    }
}
