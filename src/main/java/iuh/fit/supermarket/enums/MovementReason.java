package iuh.fit.supermarket.enums;

/**
 * Enum định nghĩa lý do xuất nhập kho trong hệ thống
 * Tương ứng với cột movement_reason trong bảng stock_movements
 */
public enum MovementReason {
    /**
     * Nhập hàng từ nhà cung cấp
     */
    PURCHASE("Purchase"),
    
    /**
     * Bán hàng cho khách
     */
    SALE("Sale"),
    
    /**
     * Trả hàng
     */
    RETURN("Return"),
    
    /**
     * Hàng bị hỏng
     */
    DAMAGE("Damage"),
    
    /**
     * Hàng hết hạn
     */
    EXPIRED("Expired"),
    
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
     * Constructor cho MovementReason
     * @param value giá trị string tương ứng trong database
     */
    MovementReason(String value) {
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
     * @return MovementReason tương ứng
     */
    public static MovementReason fromValue(String value) {
        for (MovementReason reason : MovementReason.values()) {
            if (reason.value.equals(value)) {
                return reason;
            }
        }
        throw new IllegalArgumentException("Unknown MovementReason: " + value);
    }
}
