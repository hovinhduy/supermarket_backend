package iuh.fit.supermarket.validator;

import iuh.fit.supermarket.enums.DeliveryType;
import iuh.fit.supermarket.enums.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Validator kiểm tra tính hợp lệ của việc chuyển trạng thái đơn hàng
 * Đảm bảo logic chuyển trạng thái phù hợp với loại hình nhận hàng
 */
@Component
@Slf4j
public class OrderStatusTransitionValidator {

    // Map định nghĩa các chuyển trạng thái hợp lệ cho nhận hàng tại cửa hàng
    private static final Map<OrderStatus, Set<OrderStatus>> PICKUP_TRANSITIONS = new HashMap<>();

    // Map định nghĩa các chuyển trạng thái hợp lệ cho giao hàng tận nơi
    private static final Map<OrderStatus, Set<OrderStatus>> DELIVERY_TRANSITIONS = new HashMap<>();

    static {
        // Khởi tạo luồng chuyển trạng thái cho PICKUP_AT_STORE
        // UNPAID → PENDING (sau thanh toán) → PREPARED → DELIVERED (tự động → COMPLETED)
        PICKUP_TRANSITIONS.put(OrderStatus.UNPAID,
            Set.of(OrderStatus.PENDING, OrderStatus.CANCELLED));
        PICKUP_TRANSITIONS.put(OrderStatus.PENDING,
            Set.of(OrderStatus.PREPARED, OrderStatus.CANCELLED));
        PICKUP_TRANSITIONS.put(OrderStatus.PREPARED,
            Set.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED));
        PICKUP_TRANSITIONS.put(OrderStatus.DELIVERED,
            Set.of()); // Tự động chuyển sang COMPLETED, không cần chuyển thủ công
        PICKUP_TRANSITIONS.put(OrderStatus.COMPLETED,
            Set.of()); // Không thể chuyển từ COMPLETED
        PICKUP_TRANSITIONS.put(OrderStatus.CANCELLED,
            Set.of()); // Không thể chuyển từ CANCELLED

        // Khởi tạo luồng chuyển trạng thái cho HOME_DELIVERY
        // UNPAID → PENDING (sau thanh toán) → PREPARED → SHIPPING → DELIVERED (tự động → COMPLETED)
        DELIVERY_TRANSITIONS.put(OrderStatus.UNPAID,
            Set.of(OrderStatus.PENDING, OrderStatus.CANCELLED));
        DELIVERY_TRANSITIONS.put(OrderStatus.PENDING,
            Set.of(OrderStatus.PREPARED, OrderStatus.CANCELLED));
        DELIVERY_TRANSITIONS.put(OrderStatus.PREPARED,
            Set.of(OrderStatus.SHIPPING, OrderStatus.CANCELLED));
        DELIVERY_TRANSITIONS.put(OrderStatus.SHIPPING,
            Set.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED));
        DELIVERY_TRANSITIONS.put(OrderStatus.DELIVERED,
            Set.of()); // Tự động chuyển sang COMPLETED, không cần chuyển thủ công
        DELIVERY_TRANSITIONS.put(OrderStatus.COMPLETED,
            Set.of()); // Không thể chuyển từ COMPLETED
        DELIVERY_TRANSITIONS.put(OrderStatus.CANCELLED,
            Set.of()); // Không thể chuyển từ CANCELLED
    }

    /**
     * Kiểm tra xem việc chuyển trạng thái có hợp lệ không
     *
     * @param currentStatus trạng thái hiện tại
     * @param newStatus trạng thái muốn chuyển đến
     * @param deliveryType loại hình nhận hàng
     * @return true nếu hợp lệ, false nếu không
     */
    public boolean isValidTransition(OrderStatus currentStatus, OrderStatus newStatus,
                                    DeliveryType deliveryType) {
        // Kiểm tra nếu trạng thái không thay đổi
        if (currentStatus == newStatus) {
            log.warn("Trạng thái mới giống trạng thái hiện tại: {}", currentStatus);
            return false;
        }

        // Chọn map transitions phù hợp với loại giao hàng
        Map<OrderStatus, Set<OrderStatus>> transitions =
            deliveryType == DeliveryType.PICKUP_AT_STORE ?
            PICKUP_TRANSITIONS : DELIVERY_TRANSITIONS;

        // Kiểm tra xem có thể chuyển từ currentStatus sang newStatus không
        Set<OrderStatus> allowedStatuses = transitions.get(currentStatus);
        if (allowedStatuses == null || !allowedStatuses.contains(newStatus)) {
            log.error("Không thể chuyển từ {} sang {} với loại giao hàng {}",
                currentStatus, newStatus, deliveryType);
            return false;
        }

        return true;
    }

    /**
     * Kiểm tra xem trạng thái có phù hợp với loại giao hàng không
     * Ví dụ: SHIPPING không hợp lệ cho PICKUP_AT_STORE
     *
     * @param status trạng thái cần kiểm tra
     * @param deliveryType loại hình nhận hàng
     * @return true nếu hợp lệ, false nếu không
     */
    public boolean isStatusValidForDeliveryType(OrderStatus status, DeliveryType deliveryType) {
        // SHIPPING chỉ hợp lệ cho HOME_DELIVERY
        if (status == OrderStatus.SHIPPING && deliveryType == DeliveryType.PICKUP_AT_STORE) {
            log.error("Trạng thái SHIPPING không hợp lệ cho nhận hàng tại cửa hàng");
            return false;
        }
        return true;
    }

    /**
     * Lấy danh sách trạng thái có thể chuyển đến từ trạng thái hiện tại
     *
     * @param currentStatus trạng thái hiện tại
     * @param deliveryType loại hình nhận hàng
     * @return danh sách trạng thái hợp lệ
     */
    public Set<OrderStatus> getValidNextStatuses(OrderStatus currentStatus,
                                                 DeliveryType deliveryType) {
        Map<OrderStatus, Set<OrderStatus>> transitions =
            deliveryType == DeliveryType.PICKUP_AT_STORE ?
            PICKUP_TRANSITIONS : DELIVERY_TRANSITIONS;

        return transitions.getOrDefault(currentStatus, Set.of());
    }

    /**
     * Lấy thông báo lỗi chi tiết về việc chuyển trạng thái không hợp lệ
     *
     * @param currentStatus trạng thái hiện tại
     * @param newStatus trạng thái muốn chuyển
     * @param deliveryType loại hình nhận hàng
     * @return thông báo lỗi
     */
    public String getTransitionErrorMessage(OrderStatus currentStatus, OrderStatus newStatus,
                                           DeliveryType deliveryType) {
        Set<OrderStatus> validStatuses = getValidNextStatuses(currentStatus, deliveryType);

        // Kiểm tra nếu đang cố gắng chuyển từ DELIVERED sang COMPLETED
        if (currentStatus == OrderStatus.DELIVERED && newStatus == OrderStatus.COMPLETED) {
            return "Đơn hàng sẽ tự động chuyển sang COMPLETED khi được giao. Không cần chuyển thủ công.";
        }

        if (validStatuses.isEmpty()) {
            if (currentStatus == OrderStatus.DELIVERED) {
                return "Đơn hàng đã giao sẽ tự động hoàn thành. Không cần thao tác thêm.";
            }
            if (currentStatus == OrderStatus.UNPAID) {
                return "Đơn hàng chưa thanh toán. Vui lòng thanh toán để tiếp tục.";
            }
            return String.format("Không thể chuyển từ trạng thái %s", currentStatus);
        }

        // Kiểm tra nếu SHIPPING không hợp lệ cho PICKUP_AT_STORE
        if (newStatus == OrderStatus.SHIPPING && deliveryType == DeliveryType.PICKUP_AT_STORE) {
            return "Không thể chuyển sang trạng thái SHIPPING cho đơn hàng nhận tại cửa hàng";
        }

        return String.format("Không thể chuyển từ %s sang %s. Các trạng thái hợp lệ: %s",
            currentStatus, newStatus, validStatuses);
    }
}