package iuh.fit.supermarket.enums;

/**
 * Enum định nghĩa trạng thái hóa đơn trong hệ thống
 * Tương ứng với cột status trong bảng sale_invoice_header
 */
public enum InvoiceStatus {
    /**
     * Hóa đơn chưa thanh toán (dành cho thanh toán chuyển khoản)
     */
    UNPAID("Unpaid"),

    /**
     * Hóa đơn đã thanh toán (tiền mặt hoặc chuyển khoản đã xác nhận)
     */
    PAID("Paid"),

    /**
     * Hóa đơn đã bị trả hàng
     */
    RETURNED("Returned");

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
