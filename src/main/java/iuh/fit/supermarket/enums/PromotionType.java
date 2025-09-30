package iuh.fit.supermarket.enums;

/**
 * Enum định nghĩa loại khuyến mãi trong hệ thống
 * Tương ứng với cột promotion_type trong bảng promotion_header
 */
public enum PromotionType {
    /**
     * Mua X tặng Y
     */
    BUY_X_GET_Y("BUY_X_GET_Y"),

    /**
     * Giảm giá đơn hàng
     */
    ORDER_DISCOUNT("ORDER_DISCOUNT"),

    /**
     * Giảm giá sản phẩm
     */
    PRODUCT_DISCOUNT("PRODUCT_DISCOUNT");

    private final String value;

    /**
     * Constructor cho PromotionType
     * 
     * @param value giá trị string tương ứng trong database
     */
    PromotionType(String value) {
        this.value = value;
    }

    /**
     * Lấy giá trị string của enum
     * 
     * @return giá trị string
     */
    public String getValue() {
        return value;
    }

    /**
     * Chuyển đổi từ string sang enum
     * 
     * @param value giá trị string
     * @return PromotionType tương ứng
     */
    public static PromotionType fromValue(String value) {
        for (PromotionType type : PromotionType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown PromotionType: " + value);
    }
}
