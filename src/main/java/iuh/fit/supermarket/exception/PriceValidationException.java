package iuh.fit.supermarket.exception;

/**
 * Exception cho các lỗi validation liên quan đến bảng giá
 */
public class PriceValidationException extends PriceException {

    /**
     * Constructor với message
     */
    public PriceValidationException(String message) {
        super(message);
    }

    /**
     * Constructor với message và cause
     */
    public PriceValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
