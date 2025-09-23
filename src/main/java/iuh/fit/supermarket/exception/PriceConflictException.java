package iuh.fit.supermarket.exception;

/**
 * Exception cho các lỗi xung đột business logic của bảng giá
 */
public class PriceConflictException extends PriceException {

    /**
     * Constructor với message
     */
    public PriceConflictException(String message) {
        super(message);
    }

    /**
     * Constructor với message và cause
     */
    public PriceConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor cho lỗi biến thể sản phẩm đã tồn tại trong bảng giá CURRENT khác
     */
    public static PriceConflictException variantAlreadyInCurrentPrice(String variantCode, String currentPriceCode) {
        return new PriceConflictException(
            String.format("Biến thể sản phẩm '%s' đã tồn tại trong bảng giá đang áp dụng '%s'", 
                         variantCode, currentPriceCode));
    }

    /**
     * Constructor cho lỗi không thể thay đổi trạng thái
     */
    public static PriceConflictException invalidStatusTransition(String currentStatus, String newStatus) {
        return new PriceConflictException(
            String.format("Không thể chuyển trạng thái từ '%s' sang '%s'", currentStatus, newStatus));
    }

    /**
     * Constructor cho lỗi không thể chỉnh sửa bảng giá đã active/expired
     */
    public static PriceConflictException cannotEditPrice(String status) {
        return new PriceConflictException(
            String.format("Không thể chỉnh sửa bảng giá có trạng thái '%s'", status));
    }
}
