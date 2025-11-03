package iuh.fit.supermarket.dto.checkout;

/**
 * DTO chứa thông tin khách hàng
 */
public record CustomerInfoDTO(
        /**
         * ID khách hàng
         */
        Integer customerId,

        /**
         * Tên khách hàng
         */
        String customerName,

        /**
         * Số điện thoại khách hàng
         */
        String phoneNumber,

        /**
         * Email khách hàng
         */
        String email,

        /**
         * Điểm tích lũy hiện tại
         */
        Integer currentLoyaltyPoints
) {}