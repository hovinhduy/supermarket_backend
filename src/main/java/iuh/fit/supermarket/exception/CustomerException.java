package iuh.fit.supermarket.exception;

/**
 * Exception cho các lỗi liên quan đến Customer
 */
public class CustomerException extends RuntimeException {

    public CustomerException(String message) {
        super(message);
    }

    public CustomerException(String message, Throwable cause) {
        super(message, cause);
    }
}
