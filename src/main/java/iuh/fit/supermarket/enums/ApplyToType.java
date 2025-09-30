package iuh.fit.supermarket.enums;

/**
 * Enum định nghĩa loại áp dụng khuyến mãi sản phẩm
 */
public enum ApplyToType {
    /**
     * Áp dụng cho tất cả sản phẩm
     */
    ALL("ALL"),

    /**
     * Áp dụng cho sản phẩm cụ thể
     */
    PRODUCT("PRODUCT"),

    /**
     * Áp dụng cho danh mục sản phẩm
     */
    CATEGORY("CATEGORY");

    private final String value;

    /**
     * Constructor cho ApplyToType
     * 
     * @param value giá trị string tương ứng trong database
     */
    ApplyToType(String value) {
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
     * @return ApplyToType tương ứng
     */
    public static ApplyToType fromValue(String value) {
        for (ApplyToType type : ApplyToType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ApplyToType: " + value);
    }
}
