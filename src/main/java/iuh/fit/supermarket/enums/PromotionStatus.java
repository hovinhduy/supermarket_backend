package iuh.fit.supermarket.enums;

/**
 * Enum định nghĩa loại khuyến mãi trong hệ thống
 */
public enum PromotionStatus {
    /**
     * Đang áp dụng
     */
    ACTIVE("active"),

    /**
     * Tạm dừng áp dụng
     */
    PAUSED("paused"),

    /**
     * Chưa áp dụng
     */
    UPCOMING("upcoming"),
    /**
     * Đã hết hiệu lực
     */
    EXPIRED("expired"),

    /**
     * Đã hủy
     */
    CANCELLED("cancelled");

    private final String value;

    /**
     * Constructor cho PriceType
     * 
     * @param value giá trị string tương ứng trong database
     */
    PromotionStatus(String value) {
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
    public static PromotionStatus fromValue(String value) {
        for (PromotionStatus type : PromotionStatus.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown PriceType: " + value);
    }
}
