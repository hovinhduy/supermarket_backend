package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.chat.structured.OrderInfo;

import java.util.List;

/**
 * Service interface cho tìm kiếm đơn hàng (dùng cho AI Chat)
 */
public interface OrderSearchService {

    /**
     * Lấy danh sách đơn hàng của khách hàng
     * Method này được gọi bởi AI thông qua function calling
     *
     * @param customerId ID khách hàng
     * @param limit số lượng đơn hàng tối đa (mặc định 5)
     * @return danh sách thông tin đơn hàng dạng OrderInfo
     */
    List<OrderInfo> getCustomerOrders(Integer customerId, Integer limit);

    /**
     * Lấy thông tin chi tiết đơn hàng theo ID
     *
     * @param orderId ID đơn hàng
     * @param customerId ID khách hàng (để verify ownership)
     * @return thông tin đơn hàng hoặc null nếu không tìm thấy
     */
    OrderInfo getOrderById(Long orderId, Integer customerId);
}
