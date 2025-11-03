package iuh.fit.supermarket.exception;

/**
 * Exception được ném khi request không hợp lệ
 */
public class BadRequestException extends RuntimeException {

    /**
     * Constructor với message
     * @param message thông báo lỗi
     */
    public BadRequestException(String message) {
        super(message);
    }

    /**
     * Constructor với message và cause
     * @param message thông báo lỗi
     * @param cause nguyên nhân gốc
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}