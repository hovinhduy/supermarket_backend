package iuh.fit.supermarket.exception;

/**
 * Exception khi không tìm thấy khách hàng
 */
public class CustomerNotFoundException extends CustomerException {

    public CustomerNotFoundException(String message) {
        super(message);
    }

    public CustomerNotFoundException(Integer customerId) {
        super("Không tìm thấy khách hàng với ID: " + customerId);
    }

    public CustomerNotFoundException(String field, String value) {
        super("Không tìm thấy khách hàng với " + field + ": " + value);
    }
}
