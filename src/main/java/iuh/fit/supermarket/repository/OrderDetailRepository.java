package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho quản lý chi tiết đơn hàng
 */
@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    
    /**
     * Tìm tất cả chi tiết đơn hàng theo ID đơn hàng
     *
     * @param orderId ID đơn hàng
     * @return danh sách chi tiết đơn hàng
     */
    List<OrderDetail> findByOrder_OrderId(Long orderId);
}
