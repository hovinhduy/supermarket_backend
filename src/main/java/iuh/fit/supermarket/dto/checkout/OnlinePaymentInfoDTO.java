package iuh.fit.supermarket.dto.checkout;

/**
 * DTO chứa thông tin thanh toán online
 */
public record OnlinePaymentInfoDTO(
        /**
         * Mã giao dịch thanh toán
         */
        String transactionId,

        /**
         * Phương thức thanh toán online (VNPay, Momo, ZaloPay, ...)
         */
        String paymentProvider,

        /**
         * Trạng thái thanh toán
         */
        String paymentStatus,

        /**
         * URL để redirect khách hàng đến trang thanh toán
         */
        String paymentUrl,

        /**
         * QR code để thanh toán (nếu có)
         */
        String qrCode,

        /**
         * Thời gian hết hạn thanh toán
         */
        String expirationTime
) {}