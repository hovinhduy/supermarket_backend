package iuh.fit.supermarket.exception;

/**
 * Exception cho lỗi trùng mã bảng giá
 */
public class DuplicatePriceCodeException extends PriceException {

    /**
     * Constructor với message
     */
    public DuplicatePriceCodeException(String message) {
        super(message);
    }

    /**
     * Constructor với message và cause
     */
    public DuplicatePriceCodeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor với mã bảng giá bị trùng
     */
    public DuplicatePriceCodeException(String priceCode, boolean isUpdate) {
        super(String.format("Mã bảng giá '%s' đã tồn tại trong hệ thống", priceCode));
    }
}
