package iuh.fit.supermarket.enums;

/**
 * Enum định nghĩa trạng thái thanh toán của đơn hàng
 */
public enum PaymentStatus {
    /**
     * Chưa thanh toán
     */
    UNPAID("Chưa thanh toán"),
    
    /**
     * Đã thanh toán
     */
    PAID("Đã thanh toán"),
    
    /**
     * Thanh toán thất bại
     */
    FAILED("Thanh toán thất bại"),
    
    /**
     * Đã hoàn tiền
     */
    REFUNDED("Đã hoàn tiền");
    
    private final String description;
    
    /**
     * Constructor cho PaymentStatus
     * @param description mô tả trạng thái
     */
    PaymentStatus(String description) {
        this.description = description;
    }
    
    /**
     * Lấy mô tả của trạng thái
     * @return mô tả trạng thái
     */
    public String getDescription() {
        return description;
    }
}
