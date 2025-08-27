package iuh.fit.supermarket.enums;

/**
 * Enum định nghĩa đơn vị trọng lượng trong hệ thống
 * Tương ứng với cột weight_unit trong bảng product_units
 */
public enum WeightUnit {
    /**
     * Gram
     */
    GRAM("g"),
    
    /**
     * Kilogram
     */
    KILOGRAM("kg"),
    
    /**
     * Mililít
     */
    MILLILITER("ml"),
    
    /**
     * Lít
     */
    LITER("l");
    
    private final String value;
    
    /**
     * Constructor cho WeightUnit
     * @param value giá trị string tương ứng trong database
     */
    WeightUnit(String value) {
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
     * @return WeightUnit tương ứng
     */
    public static WeightUnit fromValue(String value) {
        for (WeightUnit unit : WeightUnit.values()) {
            if (unit.value.equals(value)) {
                return unit;
            }
        }
        throw new IllegalArgumentException("Unknown WeightUnit: " + value);
    }
}
