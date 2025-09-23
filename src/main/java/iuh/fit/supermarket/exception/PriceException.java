package iuh.fit.supermarket.exception;

/**
 * Exception chung cho các lỗi liên quan đến bảng giá
 */
public class PriceException extends RuntimeException {

    /**
     * Constructor với message
     */
    public PriceException(String message) {
        super(message);
    }

    /**
     * Constructor với message và cause
     */
    public PriceException(String message, Throwable cause) {
        super(message, cause);
    }
}
