package iuh.fit.supermarket.enums;

/**
 * Enum định nghĩa trạng thái phiếu trả hàng trong hệ thống
 * Tương ứng với cột status trong bảng return_invoice_header
 */
public enum ReturnStatus {
    /**
     * Phiếu trả hàng đang chờ xử lý
     */
    PENDING("Pending"),
    
    /**
     * Phiếu trả hàng đã được phê duyệt
     */
    APPROVED("Approved"),
    
    /**
     * Phiếu trả hàng đã hoàn thành
     */
    COMPLETED("Completed"),
    
    /**
     * Phiếu trả hàng bị từ chối
     */
    REJECTED("Rejected");
    
    private final String value;
    
    /**
     * Constructor cho ReturnStatus
     * @param value giá trị string tương ứng trong database
     */
    ReturnStatus(String value) {
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
     * @return ReturnStatus tương ứng
     */
    public static ReturnStatus fromValue(String value) {
        for (ReturnStatus status : ReturnStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ReturnStatus: " + value);
    }
}
