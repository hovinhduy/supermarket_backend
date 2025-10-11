package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository cho quản lý chi tiết đơn hàng
 */
@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
}
