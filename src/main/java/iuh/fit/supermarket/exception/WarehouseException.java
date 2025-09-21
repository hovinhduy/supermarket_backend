package iuh.fit.supermarket.exception;

/**
 * Exception được ném khi có lỗi trong quá trình xử lý tồn kho
 */
public class WarehouseException extends RuntimeException {

    /**
     * Constructor với message
     */
    public WarehouseException(String message) {
        super(message);
    }

    /**
     * Constructor với message và cause
     */
    public WarehouseException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor với cause
     */
    public WarehouseException(Throwable cause) {
        super(cause);
    }
}
