package iuh.fit.supermarket.service;

import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service xử lý thanh toán online với PayOS
 */
public interface PaymentService {

    /**
     * Tạo payment link cho đơn hàng
     *
     * @param orderCode   Mã đơn hàng
     * @param amount      Số tiền thanh toán
     * @param description Mô tả thanh toán
     * @param items       Danh sách sản phẩm
     * @return CreatePaymentLinkResponse chứa payment link và QR code
     */
    CreatePaymentLinkResponse createPaymentLink(Long orderCode, BigDecimal amount, String description,
                                                List<PaymentItemData> items);

    /**
     * Xử lý thanh toán thành công từ webhook
     *
     * @param orderCode Mã đơn hàng
     */
    void handlePaymentSuccess(Long orderCode);

    /**
     * Data class cho item trong payment
     */
    record PaymentItemData(String name, int quantity, int price) {
    }
}
