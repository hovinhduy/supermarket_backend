package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.enums.DiscountType;
import iuh.fit.supermarket.enums.PromotionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity cho khuyến mãi loại Mua X Tặng Y
 * Khi khách mua sản phẩm/danh mục với số lượng/giá trị tối thiểu, được tặng sản phẩm
 */
@Entity
@DiscriminatorValue("BUY_X_GET_Y")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class BuyXGetYDetail extends PromotionDetail {

    /**
     * Sản phẩm phải mua
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buy_product_id")
    private ProductUnit buyProduct;

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

    @Override
    public PromotionType getPromotionType() {
        return PromotionType.BUY_X_GET_Y;
    }

    @Override
    public void validate() {
        if (giftProduct == null) {
            throw new IllegalArgumentException("Sản phẩm tặng không được để trống");
        }
        if (giftDiscountType == null) {
            throw new IllegalArgumentException("Loại giảm giá quà tặng không được để trống");
        }
        if (buyProduct == null) {
            throw new IllegalArgumentException("Phải chỉ định sản phẩm phải mua");
        }
        if (buyMinQuantity == null && buyMinValue == null) {
            throw new IllegalArgumentException("Phải chỉ định số lượng hoặc giá trị tối thiểu phải mua");
        }
        if (giftDiscountType != DiscountType.FREE && giftDiscountValue == null) {
            throw new IllegalArgumentException("Giá trị giảm giá quà tặng không được để trống");
        }
        if (giftDiscountType == DiscountType.PERCENTAGE && 
            (giftDiscountValue.compareTo(BigDecimal.ZERO) <= 0 || 
             giftDiscountValue.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new IllegalArgumentException("Giá trị giảm giá phần trăm phải từ 1-100");
        }
    }
}
