package iuh.fit.supermarket.enums;

/**
 * Enum định nghĩa loại giá trong hệ thống
 * Tương ứng với cột price_type trong bảng price_history
 */
public enum PriceType {
    /**
     * Đang áp dụng
     */
    ACTIVE("active"),

    /**
     * Tạm dừng áp dụng
     */
    PAUSED("paused"),
    
    /**
     * Đã hết hiệu lực
     */
    EXPIRED("expired");

    private final String value;

    /**
     * Constructor cho PriceType
     * 
     * @param value giá trị string tương ứng trong database
     */
    PriceType(String value) {
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
     * @return PriceType tương ứng
     */
    public static PriceType fromValue(String value) {
        for (PriceType type : PriceType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown PriceType: " + value);
    }
}
