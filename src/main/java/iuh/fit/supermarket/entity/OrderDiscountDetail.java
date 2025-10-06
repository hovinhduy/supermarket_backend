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
 * Entity cho khuyến mãi loại Giảm Giá Đơn Hàng
 * Áp dụng giảm giá cho toàn bộ đơn hàng khi đạt điều kiện
 */
@Entity
@DiscriminatorValue("ORDER_DISCOUNT")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrderDiscountDetail extends PromotionDetail {

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

    @Override
    public PromotionType getPromotionType() {
        return PromotionType.ORDER_DISCOUNT;
    }

    @Override
    public void validate() {
        if (orderDiscountType == null) {
            throw new IllegalArgumentException("Loại giảm giá đơn hàng không được để trống");
        }
        if (orderDiscountValue == null || orderDiscountValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá trị giảm giá đơn hàng phải lớn hơn 0");
        }
        if (orderDiscountType == DiscountType.PERCENTAGE && 
            (orderDiscountValue.compareTo(BigDecimal.ZERO) <= 0 || 
             orderDiscountValue.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new IllegalArgumentException("Giá trị giảm giá phần trăm phải từ 1-100");
        }
        if (orderMinTotalValue == null && orderMinTotalQuantity == null) {
            throw new IllegalArgumentException("Phải chỉ định giá trị hoặc số lượng tối thiểu cho đơn hàng");
        }
    }
}
