package iuh.fit.supermarket.exception;

/**
 * Exception chung cho các lỗi liên quan đến promotion
 */
public class PromotionException extends RuntimeException {
    
    public PromotionException(String message) {
        super(message);
    }
    
    public PromotionException(String message, Throwable cause) {
        super(message, cause);
    }
}
