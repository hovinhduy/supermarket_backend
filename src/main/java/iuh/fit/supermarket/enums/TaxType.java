package iuh.fit.supermarket.enums;

/**
 * Enum định nghĩa loại thuế trong hệ thống
 * Tương ứng với cột tax_type trong bảng sale_invoice_detail
 */
public enum TaxType {
    /**
     * VAT 0%
     */
    VAT_0("VAT_0"),
    
    /**
     * VAT 5%
     */
    VAT_5("VAT_5"),
    
    /**
     * VAT 10%
     */
    VAT_10("VAT_10"),
    
    /**
     * Miễn thuế VAT
     */
    VAT_EXEMPT("VAT_Exempt");
    
    private final String value;
    
    /**
     * Constructor cho TaxType
     * @param value giá trị string tương ứng trong database
     */
    TaxType(String value) {
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
     * @return TaxType tương ứng
     */
    public static TaxType fromValue(String value) {
        for (TaxType type : TaxType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown TaxType: " + value);
    }
}
