package iuh.fit.supermarket.enums;

/**
 * Enum định nghĩa loại hình nhận hàng trong hệ thống
 */
public enum DeliveryType {
    /**
     * Nhận hàng tại cửa hàng
     */
    PICKUP_AT_STORE("Pickup at Store"),

    /**
     * Giao hàng tận nơi
     */
    HOME_DELIVERY("Home Delivery");

    private final String value;

    /**
     * Constructor cho DeliveryType
     * @param value giá trị string tương ứng trong database
     */
    DeliveryType(String value) {
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
     * @return DeliveryType tương ứng
     */
    public static DeliveryType fromValue(String value) {
        for (DeliveryType type : DeliveryType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown DeliveryType: " + value);
    }
}