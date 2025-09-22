package iuh.fit.supermarket.exception;

/**
 * Exception khi không tìm thấy phiếu kiểm kê
 */
public class StocktakeNotFoundException extends StocktakeException {

    /**
     * Constructor với message
     * 
     * @param message thông báo lỗi
     */
    public StocktakeNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor với ID phiếu kiểm kê
     * 
     * @param stocktakeId ID phiếu kiểm kê
     */
    public StocktakeNotFoundException(Integer stocktakeId) {
        super("Không tìm thấy phiếu kiểm kê với ID: " + stocktakeId);
    }

}
