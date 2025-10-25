package iuh.fit.supermarket.exception;

/**
 * Lớp ngoại lệ tùy chỉnh khi có sản phẩm trùng lặp
 */
public class DuplicateProductException extends RuntimeException {

    /**
     * Khởi tạo ngoại lệ với thông báo lỗi
     * 
     * @param message thông báo lỗi
     */
    public DuplicateProductException(String message) {
        super(message);
    }

    /**
     * Khởi tạo ngoại lệ với thông báo lỗi và nguyên nhân
     * 
     * @param message thông báo lỗi
     * @param cause   nguyên nhân
     */
    public DuplicateProductException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Tạo ngoại lệ với tên sản phẩm bị trùng
     * 
     * @param productName tên sản phẩm bị trùng
     * @return instance của DuplicateProductException
     */
    public static DuplicateProductException forProductName(String productName) {
        return new DuplicateProductException("Sản phẩm với tên '" + productName + "' đã tồn tại trong hệ thống");
    }

    /**
     * Tạo ngoại lệ với mã sản phẩm bị trùng
     * 
     * @param productCode mã sản phẩm bị trùng
     * @return instance của DuplicateProductException
     */
    public static DuplicateProductException forProductCode(String productCode) {
        return new DuplicateProductException("Sản phẩm với mã '" + productCode + "' đã tồn tại trong hệ thống");
    }
}
