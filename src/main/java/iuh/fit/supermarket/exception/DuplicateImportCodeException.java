package iuh.fit.supermarket.exception;

/**
 * Exception được ném khi mã phiếu nhập đã tồn tại trong hệ thống
 */
public class DuplicateImportCodeException extends ImportException {

    private final String importCode;

    /**
     * Constructor với mã phiếu nhập bị trùng
     */
    public DuplicateImportCodeException(String importCode) {
        super(String.format("Mã phiếu nhập '%s' đã tồn tại trong hệ thống", importCode));
        this.importCode = importCode;
    }

    /**
     * Constructor với message tùy chỉnh
     */
    public DuplicateImportCodeException(String message, String importCode) {
        super(message);
        this.importCode = importCode;
    }

    /**
     * Constructor với message và cause
     */
    public DuplicateImportCodeException(String message, String importCode, Throwable cause) {
        super(message, cause);
        this.importCode = importCode;
    }

    public String getImportCode() {
        return importCode;
    }
}
