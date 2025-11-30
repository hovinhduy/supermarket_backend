package iuh.fit.supermarket.dto.checkout;

import java.time.LocalTime;

/**
 * DTO chứa thông tin cửa hàng nhận hàng
 */
public record PickupInfoDTO(
        /**
         * ID cửa hàng
         */
        Long storeId,

        /**
         * Mã cửa hàng
         */
        String storeCode,

        /**
         * Tên cửa hàng
         */
        String storeName,

        /**
         * Địa chỉ cửa hàng
         */
        String address,

        /**
         * Số điện thoại cửa hàng
         */
        String phone,

        /**
         * Giờ mở cửa
         */
        LocalTime openingTime,

        /**
         * Giờ đóng cửa
         */
        LocalTime closingTime
) {}
