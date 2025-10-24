package iuh.fit.supermarket.dto.promotion;

import iuh.fit.supermarket.enums.ApplyToType;
import iuh.fit.supermarket.enums.DiscountType;
import iuh.fit.supermarket.enums.PromotionType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * DTO cho request chi tiết khuyến mãi
 * Hỗ trợ 3 loại khuyến mãi: BUY_X_GET_Y, ORDER_DISCOUNT, PRODUCT_DISCOUNT
 * Validation sẽ được thực hiện dựa trên loại khuyến mãi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDetailRequestDTO {

    // =====================================================
    // LOẠI 1: MUA X TẶNG Y (BUY_X_GET_Y)
    // =====================================================

    /**
     * ID sản phẩm phải mua (bắt buộc cho BUY_X_GET_Y)
     */
    private Long buyProductId;

    /**
     * Số lượng tối thiểu phải mua
     */
    @Min(value = 1, message = "Số lượng tối thiểu phải mua phải là số dương")
    private Integer buyMinQuantity;

    /**
     * Giá trị tối thiểu phải mua
     */
    @DecimalMin(value = "0.01", message = "Giá trị tối thiểu phải mua phải lớn hơn 0")
    @Digits(integer = 16, fraction = 2, message = "Giá trị tối thiểu phải mua không hợp lệ")
    private BigDecimal buyMinValue;

    /**
     * ID sản phẩm được tặng (bắt buộc cho BUY_X_GET_Y)
     */
    private Long giftProductId;

    /**
     * Số lượng sản phẩm tặng cho mỗi lần đủ điều kiện
     * Ví dụ: Mua 2 tặng 3 (buyMinQuantity=2, giftQuantity=3)
     */
    @Min(value = 1, message = "Số lượng sản phẩm tặng phải là số dương")
    private Integer giftQuantity;

    /**
     * Loại giảm giá cho quà tặng
     */
    private DiscountType giftDiscountType;

    /**
     * Giá trị giảm giá cho quà tặng
     */
    @DecimalMin(value = "0.00", message = "Giá trị giảm giá quà tặng không được âm")
    @Digits(integer = 16, fraction = 2, message = "Giá trị giảm giá quà tặng không hợp lệ")
    private BigDecimal giftDiscountValue;

    /**
     * Giới hạn số lượng tặng mỗi đơn hàng
     */
    @Min(value = 1, message = "Giới hạn số lượng tặng phải là số dương")
    private Integer giftMaxQuantity;

    // =====================================================
    // LOẠI 2: GIẢM GIÁ ĐƠN HÀNG (ORDER_DISCOUNT)
    // =====================================================

    /**
     * Loại giảm giá đơn hàng (bắt buộc cho ORDER_DISCOUNT)
     */
    private DiscountType orderDiscountType;

    /**
     * Giá trị giảm giá đơn hàng
     */
    @DecimalMin(value = "0.01", message = "Giá trị giảm giá đơn hàng phải lớn hơn 0")
    @Digits(integer = 16, fraction = 2, message = "Giá trị giảm giá đơn hàng không hợp lệ")
    private BigDecimal orderDiscountValue;

    /**
     * Giới hạn giảm tối đa cho đơn hàng
     */
    @DecimalMin(value = "0.01", message = "Giới hạn giảm tối đa phải lớn hơn 0")
    @Digits(integer = 16, fraction = 2, message = "Giới hạn giảm tối đa không hợp lệ")
    private BigDecimal orderDiscountMaxValue;

    /**
     * Tổng giá trị đơn hàng tối thiểu
     */
    @DecimalMin(value = "0.01", message = "Giá trị đơn hàng tối thiểu phải lớn hơn 0")
    @Digits(integer = 16, fraction = 2, message = "Giá trị đơn hàng tối thiểu không hợp lệ")
    private BigDecimal orderMinTotalValue;

    /**
     * Tổng số lượng sản phẩm tối thiểu trong đơn hàng
     */
    @Min(value = 1, message = "Số lượng sản phẩm tối thiểu phải là số dương")
    private Integer orderMinTotalQuantity;

    // =====================================================
    // LOẠI 3: GIẢM GIÁ SẢN PHẨM (PRODUCT_DISCOUNT)
    // =====================================================

    /**
     * Loại giảm giá sản phẩm (bắt buộc cho PRODUCT_DISCOUNT)
     */
    private DiscountType productDiscountType;

    /**
     * Giá trị giảm giá sản phẩm
     */
    @DecimalMin(value = "0.01", message = "Giá trị giảm giá sản phẩm phải lớn hơn 0")
    @Digits(integer = 16, fraction = 2, message = "Giá trị giảm giá sản phẩm không hợp lệ")
    private BigDecimal productDiscountValue;

    /**
     * Áp dụng cho loại nào (bắt buộc cho PRODUCT_DISCOUNT)
     */
    private ApplyToType applyToType;

    /**
     * ID sản phẩm cụ thể được áp dụng
     */
    private Long applyToProductId;

    /**
     * Giá trị đơn hàng tối thiểu để áp dụng giảm giá sản phẩm
     */
    @DecimalMin(value = "0.01", message = "Giá trị đơn hàng tối thiểu phải lớn hơn 0")
    @Digits(integer = 16, fraction = 2, message = "Giá trị đơn hàng tối thiểu không hợp lệ")
    private BigDecimal productMinOrderValue;

    /**
     * Giá trị sản phẩm được khuyến mãi tối thiểu
     */
    @DecimalMin(value = "0.01", message = "Giá trị sản phẩm khuyến mãi tối thiểu phải lớn hơn 0")
    @Digits(integer = 16, fraction = 2, message = "Giá trị sản phẩm khuyến mãi tối thiểu không hợp lệ")
    private BigDecimal productMinPromotionValue;

    /**
     * Số lượng sản phẩm được khuyến mãi tối thiểu
     */
    @Min(value = 1, message = "Số lượng sản phẩm khuyến mãi tối thiểu phải là số dương")
    private Integer productMinPromotionQuantity;

    /**
     * Validation tùy chỉnh cho loại giảm giá phần trăm
     */
    @AssertTrue(message = "Giá trị giảm giá phần trăm phải từ 1-100")
    @JsonIgnore
    public boolean isValidPercentageDiscount() {
        // Kiểm tra gift discount
        if (giftDiscountType == DiscountType.PERCENTAGE && giftDiscountValue != null) {
            return giftDiscountValue.compareTo(BigDecimal.ONE) >= 0 &&
                    giftDiscountValue.compareTo(BigDecimal.valueOf(100)) <= 0;
        }

        // Kiểm tra order discount
        if (orderDiscountType == DiscountType.PERCENTAGE && orderDiscountValue != null) {
            return orderDiscountValue.compareTo(BigDecimal.ONE) >= 0 &&
                    orderDiscountValue.compareTo(BigDecimal.valueOf(100)) <= 0;
        }

        // Kiểm tra product discount
        if (productDiscountType == DiscountType.PERCENTAGE && productDiscountValue != null) {
            return productDiscountValue.compareTo(BigDecimal.ONE) >= 0 &&
                    productDiscountValue.compareTo(BigDecimal.valueOf(100)) <= 0;
        }

        return true;
    }
}
