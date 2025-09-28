package iuh.fit.supermarket.exception;

/**
 * Exception được ném khi không tìm thấy promotion
 */
public class PromotionNotFoundException extends PromotionException {

    public PromotionNotFoundException(String message) {
        super(message);
    }

    public PromotionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor cho trường hợp không tìm thấy promotion header theo ID
     */
    public static PromotionNotFoundException forPromotionId(Long promotionId) {
        return new PromotionNotFoundException("Không tìm thấy chương trình khuyến mãi với ID: " + promotionId);
    }

    /**
     * Constructor cho trường hợp không tìm thấy promotion line theo mã
     */
    public static PromotionNotFoundException forLineCode(String lineCode) {
        return new PromotionNotFoundException("Không tìm thấy dòng khuyến mãi với mã: " + lineCode);
    }

    /**
     * Constructor cho trường hợp không tìm thấy promotion line theo ID
     */
    public static PromotionNotFoundException forLineId(Long lineId) {
        return new PromotionNotFoundException("Không tìm thấy dòng khuyến mãi với ID: " + lineId);
    }

    /**
     * Constructor cho trường hợp không tìm thấy promotion detail theo ID
     */
    public static PromotionNotFoundException forDetailId(Long detailId) {
        return new PromotionNotFoundException("Không tìm thấy chi tiết khuyến mãi với ID: " + detailId);
    }
}
