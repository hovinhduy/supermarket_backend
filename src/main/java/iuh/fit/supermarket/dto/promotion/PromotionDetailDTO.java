package iuh.fit.supermarket.dto.promotion;

import iuh.fit.supermarket.enums.ApplyToType;
import iuh.fit.supermarket.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO đại diện cho chi tiết khuyến mãi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionDetailDTO {

    /**
     * ID duy nhất của chi tiết khuyến mãi
     */
    private Long detailId;

    /**
     * ID chương trình khuyến mãi
     */
    private Long promotionId;

    // =====================================================
    // LOẠI 1: MUA X TẶNG Y (BUY_X_GET_Y)
    // =====================================================

    /**
     * ID sản phẩm phải mua
     */
    private Long buyProductId;

    /**
     * ID danh mục phải mua
     */
    private Integer buyCategoryId;

    /**
     * Số lượng tối thiểu phải mua
     */
    private Integer buyMinQuantity;

    /**
     * Giá trị tối thiểu phải mua
     */
    private BigDecimal buyMinValue;

    /**
     * ID sản phẩm được tặng
     */
    private Long giftProductId;

    /**
     * Loại giảm giá cho quà tặng
     */
    private DiscountType giftDiscountType;

    /**
     * Giá trị giảm giá cho quà tặng
     */
    private BigDecimal giftDiscountValue;

    /**
     * Giới hạn số lượng tặng mỗi đơn hàng
     */
    private Integer giftMaxQuantity;

    // =====================================================
    // LOẠI 2: GIẢM GIÁ ĐƠN HÀNG (ORDER_DISCOUNT)
    // =====================================================

    /**
     * Loại giảm giá đơn hàng
     */
    private DiscountType orderDiscountType;

    /**
     * Giá trị giảm giá đơn hàng
     */
    private BigDecimal orderDiscountValue;

    /**
     * Giới hạn giảm tối đa cho đơn hàng
     */
    private BigDecimal orderDiscountMaxValue;

    /**
     * Tổng giá trị đơn hàng tối thiểu
     */
    private BigDecimal orderMinTotalValue;

    /**
     * Tổng số lượng sản phẩm tối thiểu trong đơn hàng
     */
    private Integer orderMinTotalQuantity;

    // =====================================================
    // LOẠI 3: GIẢM GIÁ SẢN PHẨM (PRODUCT_DISCOUNT)
    // =====================================================

    /**
     * Loại giảm giá sản phẩm
     */
    private DiscountType productDiscountType;

    /**
     * Giá trị giảm giá sản phẩm
     */
    private BigDecimal productDiscountValue;

    /**
     * Áp dụng cho loại nào
     */
    private ApplyToType applyToType;

    /**
     * ID sản phẩm cụ thể được áp dụng
     */
    private Long applyToProductId;

    /**
     * ID danh mục sản phẩm được áp dụng
     */
    private Integer applyToCategoryId;

    /**
     * Giá trị đơn hàng tối thiểu để áp dụng giảm giá sản phẩm
     */
    private BigDecimal productMinOrderValue;

    /**
     * Giá trị sản phẩm được khuyến mãi tối thiểu
     */
    private BigDecimal productMinPromotionValue;

    /**
     * Số lượng sản phẩm được khuyến mãi tối thiểu
     */
    private Integer productMinPromotionQuantity;
}

