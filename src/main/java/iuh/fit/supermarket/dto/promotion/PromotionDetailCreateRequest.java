package iuh.fit.supermarket.dto.promotion;

import iuh.fit.supermarket.enums.ApplyToType;
import iuh.fit.supermarket.enums.DiscountType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho yêu cầu tạo mới chi tiết khuyến mãi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionDetailCreateRequest {

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
    @Min(value = 1, message = "Số lượng tối thiểu phải mua phải lớn hơn 0")
    private Integer buyMinQuantity;

    /**
     * Giá trị tối thiểu phải mua
     */
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá trị tối thiểu phải lớn hơn 0")
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
    @DecimalMin(value = "0.0", message = "Giá trị giảm giá phải lớn hơn hoặc bằng 0")
    private BigDecimal giftDiscountValue;

    /**
     * Giới hạn số lượng tặng mỗi đơn hàng
     */
    @Min(value = 1, message = "Số lượng tặng phải lớn hơn 0")
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
    @DecimalMin(value = "0.0", message = "Giá trị giảm giá phải lớn hơn hoặc bằng 0")
    private BigDecimal orderDiscountValue;

    /**
     * Giới hạn giảm tối đa cho đơn hàng
     */
    @DecimalMin(value = "0.0", message = "Giá trị giảm tối đa phải lớn hơn hoặc bằng 0")
    private BigDecimal orderDiscountMaxValue;

    /**
     * Tổng giá trị đơn hàng tối thiểu
     */
    @DecimalMin(value = "0.0", message = "Giá trị đơn hàng tối thiểu phải lớn hơn hoặc bằng 0")
    private BigDecimal orderMinTotalValue;

    /**
     * Tổng số lượng sản phẩm tối thiểu trong đơn hàng
     */
    @Min(value = 1, message = "Số lượng tối thiểu phải lớn hơn 0")
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
    @DecimalMin(value = "0.0", message = "Giá trị giảm giá phải lớn hơn hoặc bằng 0")
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
    @DecimalMin(value = "0.0", message = "Giá trị đơn hàng tối thiểu phải lớn hơn hoặc bằng 0")
    private BigDecimal productMinOrderValue;

    /**
     * Giá trị sản phẩm được khuyến mãi tối thiểu
     */
    @DecimalMin(value = "0.0", message = "Giá trị sản phẩm tối thiểu phải lớn hơn hoặc bằng 0")
    private BigDecimal productMinPromotionValue;

    /**
     * Số lượng sản phẩm được khuyến mãi tối thiểu
     */
    @Min(value = 1, message = "Số lượng sản phẩm tối thiểu phải lớn hơn 0")
    private Integer productMinPromotionQuantity;
}

