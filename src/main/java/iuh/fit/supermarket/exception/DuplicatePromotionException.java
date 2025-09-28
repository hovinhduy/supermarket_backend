package iuh.fit.supermarket.exception;

/**
 * Exception được ném khi có promotion trùng lặp
 */
public class DuplicatePromotionException extends PromotionException {

    public DuplicatePromotionException(String message) {
        super(message);
    }

    public DuplicatePromotionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor cho trường hợp mã dòng khuyến mãi trùng lặp
     */
    public static DuplicatePromotionException forLineCode(String lineCode) {
        return new DuplicatePromotionException("Mã dòng khuyến mãi đã tồn tại: " + lineCode);
    }

    /**
     * Constructor cho trường hợp tên promotion trùng lặp
     */
    public static DuplicatePromotionException forPromotionName(String promotionName) {
        return new DuplicatePromotionException("Tên chương trình khuyến mãi đã tồn tại: " + promotionName);
    }
}
