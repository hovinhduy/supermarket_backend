package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.entity.Order;
import iuh.fit.supermarket.repository.OrderRepository;
import iuh.fit.supermarket.service.OrderLookupService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Implementation của OrderLookupService
 * Cung cấp thông tin đơn hàng cho AI chat
 */
@Service
@Transactional(readOnly = true)
public class OrderLookupServiceImpl implements OrderLookupService {

    private final OrderRepository orderRepository;
    private final NumberFormat currencyFormat;
    private final DateTimeFormatter dateFormatter;

    public OrderLookupServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    }

    /**
     * Lấy đơn hàng gần đây của khách hàng
     * Format output theo dạng [ORDER] structured để AI dễ parse
     */
    @Override
    public String getRecentOrders(Integer customerId, int limit) {
        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.getCustomer() != null && o.getCustomer().getCustomerId().equals(customerId))
                .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
                .limit(limit)
                .toList();

        if (orders.isEmpty()) {
            return "Khách hàng chưa có đơn hàng nào.";
        }

        StringBuilder result = new StringBuilder("Đơn hàng gần đây:\n\n");
        for (Order order : orders) {
            result.append("[ORDER]\n");
            result.append("order_id: ").append(order.getOrderId()).append("\n");
            result.append("order_code: ").append(order.getOrderCode() != null ? order.getOrderCode() : "N/A")
                    .append("\n");
            result.append("order_date: ").append(order.getOrderDate()).append("\n");
            result.append("status: ").append(translateStatus(order.getStatus().name())).append("\n");
            result.append("total_amount: ").append(order.getTotalAmount()).append("\n");

            // Thêm thông tin delivery nếu có
            if (order.getDeliveryType() != null) {
                result.append("delivery_type: ").append(translateDeliveryType(order.getDeliveryType().name()))
                        .append("\n");
            }
            if (order.getDeliveryAddress() != null && !order.getDeliveryAddress().isEmpty()) {
                result.append("delivery_address: ").append(order.getDeliveryAddress()).append("\n");
            }

            result.append("[/ORDER]\n\n");
        }

        return result.toString();
    }

    /**
     * Lấy chi tiết đơn hàng
     */
    @Override
    public String getOrderDetails(Long orderId, Integer customerId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);

        if (orderOpt.isEmpty()) {
            return "Không tìm thấy đơn hàng #" + orderId;
        }

        Order order = orderOpt.get();

        // Verify ownership
        if (order.getCustomer() == null || !order.getCustomer().getCustomerId().equals(customerId)) {
            return "Đơn hàng này không thuộc về khách hàng.";
        }

        StringBuilder details = new StringBuilder();
        details.append(String.format("Chi tiết đơn hàng #%d:\n", order.getOrderId()));
        details.append(String.format("- Ngày đặt: %s\n", order.getOrderDate().format(dateFormatter)));
        details.append(String.format("- Trạng thái: %s\n", translateStatus(order.getStatus().name())));
        details.append(String.format("- Thành tiền: %s\n", currencyFormat.format(order.getSubtotal())));
        details.append(String.format("- Tổng thanh toán: %s\n", currencyFormat.format(order.getTotalAmount())));
        details.append(String.format("- Phương thức thanh toán: %s\n",
                translatePaymentMethod(order.getPaymentMethod().name())));

        if (order.getNote() != null && !order.getNote().isEmpty()) {
            details.append(String.format("- Ghi chú: %s\n", order.getNote()));
        }

        return details.toString();
    }

    /**
     * Lấy trạng thái đơn hàng
     */
    @Override
    public String getOrderStatus(Long orderId, Integer customerId) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);

        if (orderOpt.isEmpty()) {
            return "Không tìm thấy đơn hàng #" + orderId;
        }

        Order order = orderOpt.get();

        // Verify ownership
        if (order.getCustomer() == null || !order.getCustomer().getCustomerId().equals(customerId)) {
            return "Đơn hàng này không thuộc về khách hàng.";
        }

        return String.format("Đơn hàng #%d đang ở trạng thái: %s",
                orderId, translateStatus(order.getStatus().name()));
    }

    /**
     * Dịch trạng thái đơn hàng sang tiếng Việt
     */
    private String translateStatus(String status) {
        return switch (status) {
            case "UNPAID" -> "Chưa thanh toán";
            case "PENDING" -> "Đang xử lý";
            case "CONFIRMED" -> "Đã xác nhận";
            case "PROCESSING" -> "Đang chuẩn bị";
            case "SHIPPING" -> "Đang giao hàng";
            case "DELIVERED" -> "Đã giao hàng";
            case "COMPLETED" -> "Hoàn thành";
            case "CANCELLED" -> "Đã hủy";
            default -> status;
        };
    }

    /**
     * Dịch phương thức thanh toán sang tiếng Việt
     */
    private String translatePaymentMethod(String method) {
        return switch (method) {
            case "CASH" -> "Tiền mặt";
            case "ONLINE" -> "Ngân hàng";
            default -> method;
        };
    }

    /**
     * Dịch loại hình giao hàng sang tiếng Việt
     */
    private String translateDeliveryType(String deliveryType) {
        return switch (deliveryType) {
            case "HOME_DELIVERY" -> "Giao hàng tận nơi";
            case "STORE_PICKUP" -> "Nhận tại cửa hàng";
            case "EXPRESS_DELIVERY" -> "Giao hàng nhanh";
            default -> deliveryType;
        };
    }
}
