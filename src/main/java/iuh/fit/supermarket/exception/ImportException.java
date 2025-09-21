package iuh.fit.supermarket.exception;

/**
 * Exception được ném khi có lỗi trong quá trình xử lý nhập hàng
 */
public class ImportException extends RuntimeException {

    /**
     * Constructor với message
     */
    public ImportException(String message) {
        super(message);
    }

    /**
     * Constructor với message và cause
     */
    public ImportException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor với cause
     */
    public ImportException(Throwable cause) {
        super(cause);
    }
}
