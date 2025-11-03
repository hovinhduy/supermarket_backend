package iuh.fit.supermarket.exception;

/**
 * Exception được ném khi không tìm thấy tài nguyên được yêu cầu
 */
public class NotFoundException extends RuntimeException {

    /**
     * Constructor với message
     * @param message thông báo lỗi
     */
    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor với message và cause
     * @param message thông báo lỗi
     * @param cause nguyên nhân gốc
     */
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}