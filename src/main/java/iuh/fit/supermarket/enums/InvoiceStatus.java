package iuh.fit.supermarket.enums;

/**
 * Enum định nghĩa trạng thái hóa đơn trong hệ thống
 * Tương ứng với cột status trong bảng sale_invoice_header
 */
public enum InvoiceStatus {
    /**
     * Hóa đơn nháp
     */
    DRAFT("Draft"),
    
    /**
     * Hóa đơn đã phát hành
     */
    ISSUED("Issued"),
    
    /**
     * Hóa đơn đã thanh toán
     */
    PAID("Paid"),
    
    /**
     * Hóa đơn thanh toán một phần
     */
    PARTIALLY_PAID("Partially_Paid"),
    
    /**
     * Hóa đơn đã hủy
     */
    CANCELLED("Cancelled"),
    
    /**
     * Hóa đơn đã hoàn tiền
     */
    REFUNDED("Refunded");
    
    private final String value;
    
    /**
     * Constructor cho InvoiceStatus
     * @param value giá trị string tương ứng trong database
     */
    InvoiceStatus(String value) {
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
     * @return InvoiceStatus tương ứng
     */
    public static InvoiceStatus fromValue(String value) {
        for (InvoiceStatus status : InvoiceStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown InvoiceStatus: " + value);
    }
}
