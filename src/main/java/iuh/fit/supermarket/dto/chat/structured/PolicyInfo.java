package iuh.fit.supermarket.dto.chat.structured;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Thông tin chính sách/quy định của siêu thị trong structured response
 */
public record PolicyInfo(

        /**
         * Loại chính sách: SHIPPING, RETURN, PAYMENT, OPERATING_HOURS, LOYALTY
         */
        @JsonProperty(required = true, value = "policy_type")
        String policyType,

        /**
         * Tiêu đề chính sách
         */
        @JsonProperty(required = true, value = "title")
        String title,

        /**
         * Nội dung chi tiết
         */
        @JsonProperty(value = "details")
        List<String> details,

        /**
         * Điều kiện áp dụng (nếu có)
         */
        @JsonProperty(value = "conditions")
        List<String> conditions,

        /**
         * Thông tin liên hệ để biết thêm chi tiết
         */
        @JsonProperty(value = "contact_info")
        String contactInfo
) {

    /**
     * Enum định nghĩa các loại chính sách
     */
    public enum PolicyType {
        SHIPPING,         // Chính sách giao hàng
        RETURN,           // Chính sách đổi trả
        PAYMENT,          // Phương thức thanh toán
        OPERATING_HOURS,  // Giờ hoạt động
        LOYALTY,          // Chương trình tích điểm
        GENERAL           // Chính sách chung khác
    }

    /**
     * Factory method tạo PolicyInfo cho chính sách giao hàng
     */
    public static PolicyInfo shippingPolicy() {
        return new PolicyInfo(
                PolicyType.SHIPPING.name(),
                "Chính sách giao hàng",
                List.of(
                        "Miễn phí giao hàng cho đơn từ 200,000đ",
                        "Giao hàng trong vòng 24h với đơn nội thành",
                        "Giao hàng trong 2-3 ngày với đơn ngoại thành"
                ),
                List.of("Áp dụng trong bán kính 50km"),
                "Hotline: 1900-xxxx"
        );
    }

    /**
     * Factory method tạo PolicyInfo cho chính sách đổi trả
     */
    public static PolicyInfo returnPolicy() {
        return new PolicyInfo(
                PolicyType.RETURN.name(),
                "Chính sách đổi trả",
                List.of(
                        "Đổi trả trong vòng 7 ngày",
                        "Sản phẩm phải còn nguyên vẹn, chưa qua sử dụng",
                        "Có hóa đơn mua hàng"
                ),
                List.of("Không áp dụng với thực phẩm tươi sống"),
                "Hotline: 1900-xxxx"
        );
    }

    /**
     * Factory method tạo PolicyInfo cho giờ hoạt động
     */
    public static PolicyInfo operatingHours() {
        return new PolicyInfo(
                PolicyType.OPERATING_HOURS.name(),
                "Giờ hoạt động",
                List.of(
                        "Mở cửa: 7:00 - 22:00",
                        "Tất cả các ngày trong tuần"
                ),
                null,
                null
        );
    }
}
