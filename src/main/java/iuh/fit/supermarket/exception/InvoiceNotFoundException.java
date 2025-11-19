package iuh.fit.supermarket.exception;

/**
 * Exception khi không tìm thấy hóa đơn
 */
public class InvoiceNotFoundException extends RuntimeException {

    public InvoiceNotFoundException(String message) {
        super(message);
    }

    public InvoiceNotFoundException(Integer invoiceId) {
        super("Không tìm thấy hóa đơn với ID: " + invoiceId);
    }
}
