package iuh.fit.supermarket.enums;

/**
 * Enum định nghĩa loại giảm giá trong hệ thống khuyến mãi
 */
public enum DiscountType {
    /**
     * Giảm giá theo phần trăm
     */
    PERCENTAGE("PERCENTAGE"),

    /**
     * Giảm giá cố định
     */
    FIXED_AMOUNT("FIXED_AMOUNT"),

    /**
     * Miễn phí (tặng)
     */
    FREE("FREE");

    private final String value;

    /**
     * Constructor cho DiscountType
     * 
     * @param value giá trị string tương ứng trong database
     */
    DiscountType(String value) {
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
     * @return DiscountType tương ứng
     */
    public static DiscountType fromValue(String value) {
        for (DiscountType type : DiscountType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown DiscountType: " + value);
    }
}
