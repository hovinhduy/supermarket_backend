package iuh.fit.supermarket.enums;

/**
 * Enum định nghĩa loại xuất nhập kho trong hệ thống
 * Tương ứng với cột movement_type trong bảng stock_movements
 */
public enum MovementType {
    /**
     * Nhập kho
     */
    IN("In"),
    
    /**
     * Xuất kho
     */
    OUT("Out"),
    
    /**
     * Điều chỉnh tồn kho
     */
    ADJUSTMENT("Adjustment"),
    
    /**
     * Chuyển kho
     */
    TRANSFER("Transfer");
    
    private final String value;
    
    /**
     * Constructor cho MovementType
     * @param value giá trị string tương ứng trong database
     */
    MovementType(String value) {
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
     * @return MovementType tương ứng
     */
    public static MovementType fromValue(String value) {
        for (MovementType type : MovementType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown MovementType: " + value);
    }
}
