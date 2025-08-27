package iuh.fit.supermarket.enums;

/**
 * Enum định nghĩa trạng thái kiểm kê kho trong hệ thống
 * Tương ứng với cột status trong bảng stocktakes
 */
public enum StocktakeStatus {
    /**
     * Kiểm kê đang chờ thực hiện
     */
    PENDING("Pending"),
    
    /**
     * Kiểm kê đang thực hiện
     */
    IN_PROGRESS("In_Progress"),
    
    /**
     * Kiểm kê đã hoàn thành
     */
    COMPLETED("Completed"),
    
    /**
     * Kiểm kê đã được xác nhận cuối cùng
     */
    FINALIZED("Finalized");
    
    private final String value;
    
    /**
     * Constructor cho StocktakeStatus
     * @param value giá trị string tương ứng trong database
     */
    StocktakeStatus(String value) {
        this.value = value;
    }
    
    /**
     * Lấy giá trị string của enum
     * @return giá trị string
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Chuyển đổi từ string sang enum
     * @param value giá trị string
     * @return StocktakeStatus tương ứng
     */
    public static StocktakeStatus fromValue(String value) {
        for (StocktakeStatus status : StocktakeStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown StocktakeStatus: " + value);
    }
}
