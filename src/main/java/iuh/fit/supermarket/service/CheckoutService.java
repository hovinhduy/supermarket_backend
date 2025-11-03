package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.checkout.CheckoutRequestDTO;
import iuh.fit.supermarket.dto.checkout.CheckoutResponseDTO;
import iuh.fit.supermarket.enums.OrderStatus;

/**
 * Service interface xử lý nghiệp vụ checkout giỏ hàng
 */
public interface CheckoutService {

    /**
     * Thực hiện checkout giỏ hàng cho khách hàng
     *
     * @param username username của khách hàng (email hoặc phone)
     * @param request thông tin checkout từ client
     * @return thông tin đơn hàng đã tạo
     */
    CheckoutResponseDTO checkoutForCustomer(String username, CheckoutRequestDTO request);

    /**
     * Thực hiện checkout giỏ hàng và tạo đơn hàng (deprecated - dành cho backward compatibility)
     *
     * @param request thông tin checkout từ client
     * @return thông tin đơn hàng đã tạo
     */
    @Deprecated
    CheckoutResponseDTO checkout(CheckoutRequestDTO request);

    /**
     * Cập nhật trạng thái đơn hàng
     *
     * @param orderId ID đơn hàng
     * @param newStatus trạng thái mới
     * @return thông tin đơn hàng sau khi cập nhật
     */
    CheckoutResponseDTO updateOrderStatus(Long orderId, OrderStatus newStatus);

    /**
     * Lấy thông tin chi tiết đơn hàng của khách hàng
     *
     * @param username username của khách hàng
     * @param orderId ID đơn hàng
     * @return thông tin chi tiết đơn hàng
     */
    CheckoutResponseDTO getOrderDetailForCustomer(String username, Long orderId);

    /**
     * Lấy thông tin chi tiết đơn hàng
     *
     * @param orderId ID đơn hàng
     * @return thông tin chi tiết đơn hàng
     */
    CheckoutResponseDTO getOrderDetail(Long orderId);

    /**
     * Hủy đơn hàng của khách hàng
     *
     * @param username username của khách hàng
     * @param orderId ID đơn hàng
     * @param reason lý do hủy
     * @return thông tin đơn hàng sau khi hủy
     */
    CheckoutResponseDTO cancelOrderForCustomer(String username, Long orderId, String reason);

    /**
     * Hủy đơn hàng
     *
     * @param orderId ID đơn hàng
     * @param reason lý do hủy
     * @return thông tin đơn hàng sau khi hủy
     */
    CheckoutResponseDTO cancelOrder(Long orderId, String reason);

    /**
     * Xác nhận thanh toán online thành công
     *
     * @param orderId ID đơn hàng
     * @param transactionId mã giao dịch thanh toán
     * @return thông tin đơn hàng sau khi xác nhận thanh toán
     */
    CheckoutResponseDTO confirmOnlinePayment(Long orderId, String transactionId);

    /**
     * Tính phí vận chuyển dựa trên địa chỉ giao hàng
     *
     * @param deliveryAddress địa chỉ giao hàng
     * @return phí vận chuyển
     */
    java.math.BigDecimal calculateShippingFee(String deliveryAddress);

    /**
     * Lấy danh sách đơn hàng của khách hàng với khả năng lọc theo trạng thái và phân trang
     *
     * @param username username của khách hàng
     * @param status trạng thái cần lọc (null để lấy tất cả)
     * @param pageable thông tin phân trang và sắp xếp
     * @return danh sách đơn hàng có phân trang
     */
    org.springframework.data.domain.Page<CheckoutResponseDTO> getCustomerOrders(
            String username,
            OrderStatus status,
            org.springframework.data.domain.Pageable pageable);

    /**
     * Lấy danh sách tất cả đơn hàng trong hệ thống (dành cho Admin)
     * Có khả năng lọc theo trạng thái và phân trang
     *
     * @param status trạng thái cần lọc (null để lấy tất cả)
     * @param pageable thông tin phân trang và sắp xếp
     * @return danh sách tất cả đơn hàng có phân trang
     */
    org.springframework.data.domain.Page<CheckoutResponseDTO> getAllOrders(
            OrderStatus status,
            org.springframework.data.domain.Pageable pageable);
}