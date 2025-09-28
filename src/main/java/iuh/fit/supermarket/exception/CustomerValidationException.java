package iuh.fit.supermarket.exception;

/**
 * Exception cho các lỗi validation dữ liệu khách hàng
 */
public class CustomerValidationException extends CustomerException {

    public CustomerValidationException(String message) {
        super(message);
    }

    public CustomerValidationException(String field, String message) {
        super("Lỗi validation trường '" + field + "': " + message);
    }
}
