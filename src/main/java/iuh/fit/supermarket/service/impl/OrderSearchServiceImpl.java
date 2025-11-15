package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.chat.structured.OrderInfo;
import iuh.fit.supermarket.entity.Order;
import iuh.fit.supermarket.entity.OrderDetail;
import iuh.fit.supermarket.repository.OrderRepository;
import iuh.fit.supermarket.service.OrderSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của OrderSearchService cho AI Chat
 * Tìm kiếm đơn hàng của khách hàng theo customerId
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderSearchServiceImpl implements OrderSearchService {

    private final OrderRepository orderRepository;

    /**
     * Lấy danh sách đơn hàng của khách hàng với limit
     * 
     * @param customerId ID khách hàng
     * @param limit      số lượng đơn hàng tối đa (default 5, max 20)
     * @return danh sách OrderInfo
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderInfo> getCustomerOrders(Integer customerId, Integer limit) {
        // Validate và normalize limit
        int normalizedLimit = normalizeLimit(limit);

        log.info("Searching orders for customerId: {} with limit: {}", customerId, normalizedLimit);

        // Tạo pageable với sort by orderDate DESC (đơn mới nhất trước)
        Pageable pageable = PageRequest.of(0, normalizedLimit, Sort.by(Sort.Direction.DESC, "orderDate"));

        // Query orders by customerId
        List<Order> orders = orderRepository.findByCustomerCustomerId(customerId, pageable).getContent();

        log.info("Found {} orders for customerId: {}", orders.size(), customerId);

        // Convert to OrderInfo
        return orders.stream()
                .map(this::convertToOrderInfo)
                .collect(Collectors.toList());
    }

    /**
     * Lấy thông tin chi tiết đơn hàng theo ID
     * 
     * @param orderId    ID đơn hàng
     * @param customerId ID khách hàng (verify ownership)
     * @return OrderInfo hoặc null nếu không tìm thấy/không thuộc về customer
     */
    @Override
    @Transactional(readOnly = true)
    public OrderInfo getOrderById(Long orderId, Integer customerId) {
        log.info("Getting order {} for customerId: {}", orderId, customerId);

        return orderRepository.findById(orderId)
                .filter(order -> order.getCustomer().getCustomerId().equals(customerId))
                .map(this::convertToOrderInfo)
                .orElse(null);
    }

    /**
     * Normalize limit: default 5, min 1, max 20
     */
    private int normalizeLimit(Integer limit) {
        if (limit == null || limit < 1) {
            return 5; // Default
        }
        return Math.min(limit, 20); // Max 20
    }

    /**
     * Convert Order entity sang OrderInfo DTO
     */
    private OrderInfo convertToOrderInfo(Order order) {
        // Extract order items
        List<OrderInfo.OrderItemInfo> items = null;
        if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
            items = order.getOrderDetails().stream()
                    .map(this::convertToOrderItemInfo)
                    .collect(Collectors.toList());
        }

        // Calculate item count
        int itemCount = order.getOrderDetails() != null ? order.getOrderDetails().stream()
                .mapToInt(OrderDetail::getQuantity)
                .sum() : 0;

        // Format status
        String statusDisplay = formatOrderStatus(order.getStatus().name());

        // Format delivery method
        String deliveryMethod = order.getDeliveryType() != null ? formatDeliveryType(order.getDeliveryType().name())
                : null;

        // Get delivery address
        String deliveryAddress = order.getDeliveryAddress();

        return new OrderInfo(
                order.getOrderId(),
                order.getOrderCode(),
                statusDisplay, // status
                order.getTotalAmount(),
                order.getOrderDate(),
                null, // estimated_delivery - Order entity không có field này
                deliveryMethod,
                deliveryAddress,
                itemCount,
                items);
    }

    /**
     * Convert OrderDetail sang OrderItemInfo
     */
    private OrderInfo.OrderItemInfo convertToOrderItemInfo(OrderDetail detail) {
        // Lấy tên sản phẩm từ ProductUnit
        String productName = "Sản phẩm";
        if (detail.getProductUnit() != null && detail.getProductUnit().getProduct() != null) {
            productName = detail.getProductUnit().getProduct().getName();
            // Thêm đơn vị nếu có
            if (detail.getProductUnit().getUnit() != null) {
                productName += " (" + detail.getProductUnit().getUnit().getName() + ")";
            }
        }

        return new OrderInfo.OrderItemInfo(
                productName,
                detail.getQuantity(),
                detail.getPriceAtPurchase());
    }

    /**
     * Format order status sang tiếng Việt
     */
    private String formatOrderStatus(String status) {
        return switch (status) {
            case "UNPAID" -> "Chưa thanh toán";
            case "PAID" -> "Đã thanh toán";
            case "PENDING" -> "Chờ xác nhận";
            case "CONFIRMED" -> "Đã xác nhận";
            case "PROCESSING" -> "Đang xử lý";
            case "SHIPPING" -> "Đang giao hàng";
            case "DELIVERED" -> "Đã giao hàng";
            case "COMPLETED" -> "Hoàn thành";
            case "CANCELLED" -> "Đã hủy";
            case "RETURNED" -> "Đã trả hàng";
            default -> status;
        };
    }

    /**
     * Format delivery type sang tiếng Việt
     */
    private String formatDeliveryType(String type) {
        return switch (type) {
            case "HOME_DELIVERY" -> "Giao hàng tận nhà";
            case "STORE_PICKUP" -> "Nhận tại cửa hàng";
            case "EXPRESS" -> "Giao hàng nhanh";
            default -> type;
        };
    }
}
