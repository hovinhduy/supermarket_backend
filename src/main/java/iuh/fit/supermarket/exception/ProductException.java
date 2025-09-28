package iuh.fit.supermarket.exception;

/**
 * Lớp ngoại lệ tùy chỉnh cho các lỗi liên quan đến sản phẩm
 */
public class ProductException extends RuntimeException {

    /**
     * Khởi tạo ngoại lệ với thông báo lỗi
     * 
     * @param message thông báo lỗi
     */
    public ProductException(String message) {
        super(message);
    }

    /**
     * Khởi tạo ngoại lệ với thông báo lỗi và nguyên nhân
     * 
     * @param message thông báo lỗi
     * @param cause   nguyên nhân
     */
    public ProductException(String message, Throwable cause) {
        super(message, cause);
    }
}
