package iuh.fit.supermarket.enums;

/**
 * Enum định nghĩa loại chi tiết khuyến mãi trong hệ thống
 * Tương ứng với cột detail_type trong bảng promotion_details
 */
public enum PromotionType {
    /**
     * Giảm giá theo phần trăm cho toàn đơn hàng
     */
    PERCENT_ORDER("PERCENT_ORDER"),

    /**
     * Giảm giá cố định cho toàn đơn hàng
     */
    FIXED_ORDER("FIXED_ORDER"),

    /**
     * Giảm giá theo phần trăm cho sản phẩm
     */
    PERCENT_PRODUCT("PERCENT_PRODUCT"),

    /**
     * Giảm giá cố định cho sản phẩm
     */
    FIXED_PRODUCT("FIXED_PRODUCT"),

    /**
     * Mua X tặng Y
     */
    BUY_X_GET_Y("BUY_X_GET_Y");

    private final String value;

    /**
     * Constructor cho PromotionDetailType
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
     * @return PromotionDetailType tương ứng
     */
    public static PromotionType fromValue(String value) {
        for (PromotionType type : PromotionType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown PromotionDetailType: " + value);
    }
}
