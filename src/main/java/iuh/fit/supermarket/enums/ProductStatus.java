package iuh.fit.supermarket.enums;

/**
 * Enum định nghĩa trạng thái sản phẩm trong hệ thống
 * Tương ứng với cột status trong bảng products
 */
public enum ProductStatus {
    /**
     * Sản phẩm đang hoạt động
     */
    ACTIVE("Active"),
    
    /**
     * Sản phẩm tạm ngừng hoạt động
     */
    INACTIVE("Inactive"),
    
    /**
     * Sản phẩm đã ngừng kinh doanh
     */
    DISCONTINUED("Discontinued");
    
    private final String value;
    
    /**
     * Constructor cho ProductStatus
     * @param value giá trị string tương ứng trong database
     */
    ProductStatus(String value) {
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
     * @return ProductStatus tương ứng
     */
    public static ProductStatus fromValue(String value) {
        for (ProductStatus status : ProductStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ProductStatus: " + value);
    }
}
