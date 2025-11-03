package iuh.fit.supermarket.exception;

/**
 * Exception được ném khi người dùng không có quyền thực hiện thao tác
 */
public class UnauthorizedException extends RuntimeException {

    /**
     * Constructor với message
     * @param message thông báo lỗi
     */
    public UnauthorizedException(String message) {
        super(message);
    }

    /**
     * Constructor với message và cause
     * @param message thông báo lỗi
     * @param cause nguyên nhân gốc
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}