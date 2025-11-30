package iuh.fit.supermarket.enums;

/**
 * Enum đại diện cho nhãn địa chỉ giao hàng
 */
public enum AddressLabel {
    /**
     * Nhà riêng
     */
    HOME("Nhà"),

    /**
     * Văn phòng
     */
    OFFICE("Văn phòng"),

    /**
     * Tòa nhà
     */
    BUILDING("Tòa nhà");

    private final String displayName;

    AddressLabel(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Lấy tên hiển thị
     */
    public String getDisplayName() {
        return displayName;
    }
}
