package iuh.fit.supermarket.exception;

/**
 * Exception được ném khi mã phiếu kiểm kê đã tồn tại trong hệ thống
 */
public class DuplicateStocktakeCodeException extends StocktakeException {

    private final String stocktakeCode;

    /**
     * Constructor với mã phiếu kiểm kê bị trùng
     * 
     * @param stocktakeCode mã phiếu kiểm kê bị trùng
     */
    public DuplicateStocktakeCodeException(String stocktakeCode) {
        super(String.format("Mã phiếu kiểm kê '%s' đã tồn tại trong hệ thống", stocktakeCode));
        this.stocktakeCode = stocktakeCode;
    }

    /**
     * Constructor với message tùy chỉnh
     * 
     * @param message       thông báo lỗi tùy chỉnh
     * @param stocktakeCode mã phiếu kiểm kê bị trùng
     */
    public DuplicateStocktakeCodeException(String message, String stocktakeCode) {
        super(message);
        this.stocktakeCode = stocktakeCode;
    }

    /**
     * Constructor với message và cause
     * 
     * @param message       thông báo lỗi
     * @param stocktakeCode mã phiếu kiểm kê bị trùng
     * @param cause         nguyên nhân gây lỗi
     */
    public DuplicateStocktakeCodeException(String message, String stocktakeCode, Throwable cause) {
        super(message, cause);
        this.stocktakeCode = stocktakeCode;
    }

    /**
     * Lấy mã phiếu kiểm kê bị trùng
     * 
     * @return mã phiếu kiểm kê
     */
    public String getStocktakeCode() {
        return stocktakeCode;
    }
}
