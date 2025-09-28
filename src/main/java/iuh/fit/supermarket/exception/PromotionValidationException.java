package iuh.fit.supermarket.exception;

/**
 * Exception được ném khi dữ liệu promotion không hợp lệ
 */
public class PromotionValidationException extends PromotionException {
    
    public PromotionValidationException(String message) {
        super(message);
    }
    
    public PromotionValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
