package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository cho quản lý đơn hàng
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}
