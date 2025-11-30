package iuh.fit.supermarket.dto.checkout;

import iuh.fit.supermarket.enums.DeliveryType;
import iuh.fit.supermarket.enums.PaymentMethod;
import jakarta.validation.constraints.*;

/**
 * DTO cho yêu cầu checkout giỏ hàng của khách hàng
 */
public record CheckoutRequestDTO(

        /**
         * Loại hình nhận hàng
         */
        @NotNull(message = "Loại hình nhận hàng không được để trống")
        DeliveryType deliveryType,

        /**
         * Phương thức thanh toán
         */
        @NotNull(message = "Phương thức thanh toán không được để trống")
        PaymentMethod paymentMethod,

        /**
         * ID địa chỉ giao hàng từ danh sách địa chỉ đã lưu của khách hàng
         * Bắt buộc khi chọn giao hàng tận nơi (HOME_DELIVERY)
         */
        Long addressId,

        /**
         * Ghi chú đơn hàng
         */
        String orderNote
) {
    /**
     * Constructor với validation
     */
    public CheckoutRequestDTO {
        // Validate nếu chọn giao hàng tận nơi thì bắt buộc phải có addressId
        if (deliveryType == DeliveryType.HOME_DELIVERY && addressId == null) {
            throw new IllegalArgumentException("Vui lòng chọn địa chỉ giao hàng từ danh sách địa chỉ đã lưu");
        }
    }
}