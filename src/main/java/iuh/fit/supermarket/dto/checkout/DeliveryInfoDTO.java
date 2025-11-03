package iuh.fit.supermarket.dto.checkout;

import jakarta.validation.constraints.*;

/**
 * DTO chứa thông tin giao hàng
 */
public record DeliveryInfoDTO(
        /**
         * Tên người nhận hàng
         */
        @NotBlank(message = "Tên người nhận không được để trống")
        @Size(max = 255, message = "Tên người nhận không được vượt quá 255 ký tự")
        String recipientName,

        /**
         * Số điện thoại nhận hàng
         */
        @NotBlank(message = "Số điện thoại không được để trống")
        @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
        String deliveryPhone,

        /**
         * Địa chỉ giao hàng
         */
        @NotBlank(message = "Địa chỉ giao hàng không được để trống")
        String deliveryAddress,

        /**
         * Ghi chú giao hàng
         */
        String deliveryNote
) {
    /**
     * Constructor với validation
     */
    public DeliveryInfoDTO {
        // Trim các giá trị string
        if (recipientName != null) {
            recipientName = recipientName.trim();
        }
        if (deliveryPhone != null) {
            deliveryPhone = deliveryPhone.trim();
        }
        if (deliveryAddress != null) {
            deliveryAddress = deliveryAddress.trim();
        }
        if (deliveryNote != null) {
            deliveryNote = deliveryNote.trim();
        }
    }
}