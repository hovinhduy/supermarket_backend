package iuh.fit.supermarket.dto.checkout;

import iuh.fit.supermarket.enums.DeliveryType;
import iuh.fit.supermarket.enums.OrderStatus;
import iuh.fit.supermarket.enums.PaymentMethod;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho response của checkout
 */
public record CheckoutResponseDTO(
        /**
         * ID của đơn hàng đã tạo
         */
        Long orderId,

        /**
         * Mã đơn hàng
         */
        String orderCode,

        /**
         * Trạng thái đơn hàng
         */
        OrderStatus orderStatus,

        /**
         * Loại hình nhận hàng
         */
        DeliveryType deliveryType,

        /**
         * Phương thức thanh toán
         */
        PaymentMethod paymentMethod,

        /**
         * Mã giao dịch thanh toán
         */
        String transactionId,

        /**
         * Thông tin khách hàng
         */
        CustomerInfoDTO customerInfo,

        /**
         * Thông tin giao hàng (nếu có)
         */
        DeliveryInfoDTO deliveryInfo,

        /**
         * Thông tin cửa hàng nhận hàng (nếu PICKUP_AT_STORE)
         */
        PickupInfoDTO pickupInfo,

        /**
         * Danh sách sản phẩm trong đơn hàng
         */
        List<OrderItemDTO> orderItems,

        /**
         * Tổng tiền hàng (chưa giảm giá)
         */
        BigDecimal subtotal,

        /**
         * Tổng tiền giảm giá
         */
        BigDecimal totalDiscount,

        /**
         * Phí vận chuyển
         */
        BigDecimal shippingFee,

        /**
         * Điểm tích lũy đã sử dụng
         */
        Integer loyaltyPointsUsed,

        /**
         * Số tiền được giảm từ điểm tích lũy
         */
        BigDecimal loyaltyPointsDiscount,

        /**
         * Tổng tiền phải thanh toán
         */
        BigDecimal totalAmount,

        /**
         * Số tiền khách trả (cho thanh toán tiền mặt)
         */
        BigDecimal amountPaid,

        /**
         * Tiền thừa trả lại (cho thanh toán tiền mặt)
         */
        BigDecimal changeAmount,

        /**
         * Thông tin thanh toán online (nếu có)
         */
        OnlinePaymentInfoDTO onlinePaymentInfo,

        /**
         * Danh sách khuyến mãi đã áp dụng
         */
        List<PromotionAppliedDTO> appliedPromotions,

        /**
         * Điểm tích lũy nhận được từ đơn hàng
         */
        Integer loyaltyPointsEarned,

        /**
         * Thời gian tạo đơn hàng
         */
        LocalDateTime createdAt,

        /**
         * Thông báo
         */
        String message
) {}