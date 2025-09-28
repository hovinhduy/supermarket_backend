package iuh.fit.supermarket.exception;

/**
 * Lớp ngoại lệ tùy chỉnh khi không tìm thấy sản phẩm
 */
public class ProductNotFoundException extends RuntimeException {

    /**
     * Khởi tạo ngoại lệ với thông báo lỗi
     * 
     * @param message thông báo lỗi
     */
    public ProductNotFoundException(String message) {
        super(message);
    }

    /**
     * Khởi tạo ngoại lệ với thông báo lỗi và nguyên nhân
     * 
     * @param message thông báo lỗi
     * @param cause   nguyên nhân
     */
    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Khởi tạo ngoại lệ với ID sản phẩm không tìm thấy
     * 
     * @param productId ID sản phẩm
     */
    public ProductNotFoundException(Long productId) {
        super("Không tìm thấy sản phẩm với ID: " + productId);
    }
}
