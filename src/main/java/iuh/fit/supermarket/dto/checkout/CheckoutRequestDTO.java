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
         * Địa chỉ nhận hàng (bắt buộc nếu chọn giao hàng tận nơi)
         */
        String deliveryAddress,

        /**
         * Ghi chú đơn hàng
         */
        String orderNote
) {
    /**
     * Constructor với validation
     */
    public CheckoutRequestDTO {
        // Validate nếu chọn giao hàng tận nơi thì phải có địa chỉ
        if (deliveryType == DeliveryType.HOME_DELIVERY) {
            if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
                throw new IllegalArgumentException("Địa chỉ nhận hàng là bắt buộc khi chọn giao hàng tận nơi");
            }
        }
    }
}