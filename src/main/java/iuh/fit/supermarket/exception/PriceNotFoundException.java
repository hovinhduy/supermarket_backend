package iuh.fit.supermarket.exception;

/**
 * Exception cho lỗi không tìm thấy bảng giá
 */
public class PriceNotFoundException extends PriceException {

    /**
     * Constructor với message
     */
    public PriceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor với message và cause
     */
    public PriceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor với ID bảng giá không tìm thấy
     */
    public PriceNotFoundException(Long priceId) {
        super(String.format("Không tìm thấy bảng giá với ID: %d", priceId));
    }

    /**
     * Tạo exception cho trường hợp không tìm thấy bảng giá theo mã
     */
    public static PriceNotFoundException byPriceCode(String priceCode) {
        return new PriceNotFoundException(String.format("Không tìm thấy bảng giá với mã: %s", priceCode));
    }
}
