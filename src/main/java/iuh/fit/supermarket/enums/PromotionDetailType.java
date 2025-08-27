package iuh.fit.supermarket.enums;

/**
 * Enum định nghĩa loại chi tiết khuyến mãi trong hệ thống
 * Tương ứng với cột detail_type trong bảng promotion_details
 */
public enum PromotionDetailType {
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
    BUY_X_GET_Y("BUY_X_GET_Y"),
    
    /**
     * Miễn phí vận chuyển
     */
    FREE_SHIP("FREE_SHIP");
    
    private final String value;
    
    /**
     * Constructor cho PromotionDetailType
     * @param value giá trị string tương ứng trong database
     */
    PromotionDetailType(String value) {
        this.value = value;
    }
    
    /**
     * Lấy giá trị string của enum
     * @return giá trị string
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Chuyển đổi từ string sang enum
     * @param value giá trị string
     * @return PromotionDetailType tương ứng
     */
    public static PromotionDetailType fromValue(String value) {
        for (PromotionDetailType type : PromotionDetailType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown PromotionDetailType: " + value);
    }
}
