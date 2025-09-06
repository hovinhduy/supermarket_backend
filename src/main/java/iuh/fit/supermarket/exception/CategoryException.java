package iuh.fit.supermarket.exception;

/**
 * Lớp ngoại lệ tùy chỉnh cho các lỗi liên quan đến danh mục
 */
public class CategoryException extends RuntimeException {

    /**
     * Khởi tạo ngoại lệ với thông báo lỗi
     * 
     * @param message thông báo lỗi
     */
    public CategoryException(String message) {
        super(message);
    }

    /**
     * Khởi tạo ngoại lệ với thông báo lỗi và nguyên nhân
     * 
     * @param message thông báo lỗi
     * @param cause nguyên nhân
     */
    public CategoryException(String message, Throwable cause) {
        super(message, cause);
    }
}