package iuh.fit.supermarket.enums;

/**
 * Enum định nghĩa trạng thái đơn hàng trong hệ thống
 * Tương ứng với cột status trong bảng orders
 */
public enum OrderStatus {
    /**
     * Đơn hàng chưa thanh toán
     */
    UNPAID("Unpaid"),

    /**
     * Đơn hàng đang chờ xử lý
     */
    PENDING("Pending"),

    /**
     * Đơn hàng đã chuẩn bị xong
     */
    PREPARED("Prepared"),

    /**
     * Đơn hàng đang giao hàng
     */
    SHIPPING("Shipping"),

    /**
     * Đơn hàng đã giao
     */
    DELIVERED("Delivered"),

    /**
     * Đơn hàng đã hoàn thành
     */
    COMPLETED("Completed"),

    /**
     * Đơn hàng đã bị hủy
     */
    CANCELLED("Cancelled");
    
    private final String value;
    
    /**
     * Constructor cho OrderStatus
     * @param value giá trị string tương ứng trong database
     */
    OrderStatus(String value) {
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
     * @return OrderStatus tương ứng
     */
    public static OrderStatus fromValue(String value) {
        for (OrderStatus status : OrderStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown OrderStatus: " + value);
    }
}
