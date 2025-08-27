package iuh.fit.supermarket.enums;

/**
 * Enum định nghĩa phương thức thanh toán trong hệ thống
 * Tương ứng với cột payment_method trong bảng orders
 */
public enum PaymentMethod {
    /**
     * Thanh toán bằng tiền mặt
     */
    CASH("Cash"),
    
    /**
     * Thanh toán bằng thẻ
     */
    CARD("Card"),
    
    /**
     * Thanh toán trực tuyến
     */
    ONLINE("Online");
    
    private final String value;
    
    /**
     * Constructor cho PaymentMethod
     * @param value giá trị string tương ứng trong database
     */
    PaymentMethod(String value) {
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
     * @return PaymentMethod tương ứng
     */
    public static PaymentMethod fromValue(String value) {
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.value.equals(value)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Unknown PaymentMethod: " + value);
    }
}
