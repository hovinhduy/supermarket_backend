package iuh.fit.supermarket.exception;

/**
 * Exception ném ra khi dữ liệu bán hàng không hợp lệ
 */
public class InvalidSaleDataException extends RuntimeException {
    
    public InvalidSaleDataException(String message) {
        super(message);
    }
    
    public InvalidSaleDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
