package iuh.fit.supermarket.exception;

/**
 * Exception được ném khi mã phiếu nhập đạt tới giới hạn tối đa (PN999999)
 */
public class ImportCodeOverflowException extends ImportException {

    private static final String MAX_IMPORT_CODE = "PN999999";

    /**
     * Constructor mặc định
     */
    public ImportCodeOverflowException() {
        super(String.format("Đã đạt tới giới hạn tối đa mã phiếu nhập (%s). " +
                           "Vui lòng liên hệ quản trị viên để xử lý.", MAX_IMPORT_CODE));
    }

    /**
     * Constructor với message tùy chỉnh
     */
    public ImportCodeOverflowException(String message) {
        super(message);
    }

    /**
     * Constructor với message và cause
     */
    public ImportCodeOverflowException(String message, Throwable cause) {
        super(message, cause);
    }

    public static String getMaxImportCode() {
        return MAX_IMPORT_CODE;
    }
}
