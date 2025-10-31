package iuh.fit.supermarket.service;

/**
 * Service interface cho tra cứu đơn hàng (dành cho AI chat)
 */
public interface OrderLookupService {

    /**
     * Lấy thông tin đơn hàng gần đây của khách hàng
     * 
     * @param customerId ID khách hàng
     * @param limit số lượng đơn hàng cần lấy
     * @return thông tin đơn hàng dạng text cho AI
     */
    String getRecentOrders(Integer customerId, int limit);

    /**
     * Tra cứu đơn hàng theo ID
     * 
     * @param orderId ID đơn hàng
     * @param customerId ID khách hàng (để verify ownership)
     * @return thông tin chi tiết đơn hàng
     */
    String getOrderDetails(Long orderId, Integer customerId);

    /**
     * Kiểm tra trạng thái đơn hàng
     * 
     * @param orderId ID đơn hàng
     * @param customerId ID khách hàng
     * @return trạng thái đơn hàng
     */
    String getOrderStatus(Long orderId, Integer customerId);
}
