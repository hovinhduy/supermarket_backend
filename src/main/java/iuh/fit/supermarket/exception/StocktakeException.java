package iuh.fit.supermarket.exception;

/**
 * Exception cho các lỗi liên quan đến kiểm kê kho
 */
public class StocktakeException extends RuntimeException {

    /**
     * Constructor với message
     * 
     * @param message thông báo lỗi
     */
    public StocktakeException(String message) {
        super(message);
    }

    /**
     * Constructor với message và cause
     * 
     * @param message thông báo lỗi
     * @param cause nguyên nhân gây lỗi
     */
    public StocktakeException(String message, Throwable cause) {
        super(message, cause);
    }
}
