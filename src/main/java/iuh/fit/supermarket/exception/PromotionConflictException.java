package iuh.fit.supermarket.exception;

/**
 * Exception được ném khi có xung đột giữa các promotion
 */
public class PromotionConflictException extends PromotionException {
    
    public PromotionConflictException(String message) {
        super(message);
    }
    
    public PromotionConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
