package iuh.fit.supermarket.exception;

/**
 * Exception khi khách hàng cố gắng truy cập hóa đơn không thuộc về mình
 */
public class InvoiceAccessDeniedException extends RuntimeException {

    public InvoiceAccessDeniedException(String message) {
        super(message);
    }

    public InvoiceAccessDeniedException(Integer invoiceId) {
        super("Bạn không có quyền truy cập hóa đơn với ID: " + invoiceId);
    }
}
