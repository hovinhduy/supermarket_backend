package iuh.fit.supermarket.dto.promotion;

import iuh.fit.supermarket.enums.ApplyToType;
import iuh.fit.supermarket.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho response chi tiết khuyến mãi
 * Chứa đầy đủ thông tin chi tiết cho từng loại khuyến mãi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDetailResponseDTO {

    /**
     * ID duy nhất của chi tiết khuyến mãi
     */
    private Long detailId;

    // =====================================================
    // LOẠI 1: MUA X TẶNG Y (BUY_X_GET_Y)
    // =====================================================

    /**
     * Thông tin sản phẩm phải mua
     */
    private ProductUnitInfo buyProduct;

    /**
     * Số lượng tối thiểu phải mua
     */
    private Integer buyMinQuantity;

    /**
     * Giá trị tối thiểu phải mua
     */
    private BigDecimal buyMinValue;

    /**
     * Thông tin sản phẩm được tặng
     */
    private ProductUnitInfo giftProduct;

    /**
     * Số lượng sản phẩm tặng cho mỗi lần đủ điều kiện
     */
    private Integer giftQuantity;

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
     * Thông tin sản phẩm cụ thể được áp dụng
     */
    private ProductUnitInfo applyToProduct;

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

    // =====================================================
    // NESTED CLASSES CHO THÔNG TIN LIÊN KẾT
    // =====================================================

    /**
     * Thông tin cơ bản về ProductUnit
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductUnitInfo {
        private Long productUnitId;
        private String productName;
        private String unitName;
        private String variantCode;
        private BigDecimal currentPrice;
        private String imageUrl;
    }

    /**
     * Tạo mô tả ngắn gọn về khuyến mãi dựa trên loại
     */
    public String getPromotionSummary() {
        if (giftProduct != null) {
            // BUY_X_GET_Y
            StringBuilder summary = new StringBuilder("Mua ");
            if (buyProduct != null) {
                summary.append(buyProduct.getProductName());
            }
            
            if (buyMinQuantity != null) {
                summary.append(" (tối thiểu ").append(buyMinQuantity).append(" sản phẩm)");
            }
            if (buyMinValue != null) {
                summary.append(" (tối thiểu ").append(buyMinValue).append("đ)");
            }
            
            summary.append(" - Tặng ").append(giftProduct.getProductName());
            
            if (giftDiscountType == DiscountType.FREE) {
                summary.append(" (miễn phí)");
            } else if (giftDiscountType == DiscountType.PERCENTAGE) {
                summary.append(" (giảm ").append(giftDiscountValue).append("%)");
            } else if (giftDiscountType == DiscountType.FIXED_AMOUNT) {
                summary.append(" (giảm ").append(giftDiscountValue).append("đ)");
            }
            
            return summary.toString();
            
        } else if (orderDiscountType != null) {
            // ORDER_DISCOUNT
            StringBuilder summary = new StringBuilder("Giảm giá đơn hàng ");
            if (orderDiscountType == DiscountType.PERCENTAGE) {
                summary.append(orderDiscountValue).append("%");
                if (orderDiscountMaxValue != null) {
                    summary.append(" (tối đa ").append(orderDiscountMaxValue).append("đ)");
                }
            } else {
                summary.append(orderDiscountValue).append("đ");
            }
            
            if (orderMinTotalValue != null) {
                summary.append(" - Đơn hàng tối thiểu ").append(orderMinTotalValue).append("đ");
            }
            
            return summary.toString();
            
        } else if (productDiscountType != null) {
            // PRODUCT_DISCOUNT
            StringBuilder summary = new StringBuilder("Giảm giá sản phẩm ");
            if (productDiscountType == DiscountType.PERCENTAGE) {
                summary.append(productDiscountValue).append("%");
            } else {
                summary.append(productDiscountValue).append("đ");
            }
            
            if (applyToType == ApplyToType.PRODUCT && applyToProduct != null) {
                summary.append(" cho ").append(applyToProduct.getProductName());
            } else if (applyToType == ApplyToType.ALL) {
                summary.append(" cho tất cả sản phẩm");
            }
            
            return summary.toString();
        }
        
        return "Khuyến mãi đặc biệt";
    }
}
