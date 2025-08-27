package iuh.fit.supermarket.enums;

/**
 * Enum định nghĩa các loại khách hàng trong hệ thống
 * Tương ứng với cột customer_type trong bảng customers
 */
public enum CustomerType {
    /**
     * Khách hàng thường
     */
    REGULAR("Regular"),
    
    /**
     * Khách hàng VIP
     */
    VIP("VIP");
    
    private final String value;
    
    /**
     * Constructor cho CustomerType
     * @param value giá trị string tương ứng trong database
     */
    CustomerType(String value) {
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
     * @return CustomerType tương ứng
     */
    public static CustomerType fromValue(String value) {
        for (CustomerType type : CustomerType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown CustomerType: " + value);
    }
}
