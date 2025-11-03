package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.Order;
import iuh.fit.supermarket.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository cho quản lý đơn hàng
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Lấy danh sách đơn hàng của khách hàng theo username (email hoặc phone) với phân trang
     *
     * @param username username của khách hàng
     * @param pageable thông tin phân trang và sắp xếp
     * @return danh sách đơn hàng có phân trang
     */
    @Query("SELECT o FROM Order o WHERE o.customer.user.email = :username OR o.customer.user.phone = :username")
    Page<Order> findByCustomerUsername(@Param("username") String username, Pageable pageable);

    /**
     * Lấy danh sách đơn hàng của khách hàng theo username và trạng thái với phân trang
     *
     * @param username username của khách hàng
     * @param status trạng thái đơn hàng
     * @param pageable thông tin phân trang và sắp xếp
     * @return danh sách đơn hàng có phân trang
     */
    @Query("SELECT o FROM Order o WHERE (o.customer.user.email = :username OR o.customer.user.phone = :username) AND o.status = :status")
    Page<Order> findByCustomerUsernameAndStatus(@Param("username") String username, @Param("status") OrderStatus status, Pageable pageable);

    /**
     * Lấy danh sách đơn hàng theo trạng thái với phân trang
     *
     * @param status trạng thái đơn hàng
     * @param pageable thông tin phân trang và sắp xếp
     * @return danh sách đơn hàng theo trạng thái có phân trang
     */
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
}
