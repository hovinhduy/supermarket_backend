package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.Order;
import iuh.fit.supermarket.enums.OrderStatus;
import iuh.fit.supermarket.enums.DeliveryType;
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

    /**
     * Lấy danh sách đơn hàng theo loại hình nhận hàng với phân trang
     *
     * @param deliveryType loại hình nhận hàng
     * @param pageable thông tin phân trang và sắp xếp
     * @return danh sách đơn hàng theo loại hình nhận hàng có phân trang
     */
    Page<Order> findByDeliveryType(DeliveryType deliveryType, Pageable pageable);

    /**
     * Lấy danh sách đơn hàng theo trạng thái và loại hình nhận hàng với phân trang
     *
     * @param status trạng thái đơn hàng
     * @param deliveryType loại hình nhận hàng
     * @param pageable thông tin phân trang và sắp xếp
     * @return danh sách đơn hàng có phân trang
     */
    Page<Order> findByStatusAndDeliveryType(OrderStatus status, DeliveryType deliveryType, Pageable pageable);

    /**
     * Lấy danh sách đơn hàng loại trừ một trạng thái với phân trang
     *
     * @param status trạng thái đơn hàng cần loại trừ
     * @param pageable thông tin phân trang và sắp xếp
     * @return danh sách đơn hàng loại trừ trạng thái đã chỉ định có phân trang
     */
    Page<Order> findByStatusNot(OrderStatus status, Pageable pageable);

    /**
     * Lấy danh sách đơn hàng loại trừ một trạng thái và lọc theo loại hình nhận hàng với phân trang
     *
     * @param status trạng thái đơn hàng cần loại trừ
     * @param deliveryType loại hình nhận hàng
     * @param pageable thông tin phân trang và sắp xếp
     * @return danh sách đơn hàng có phân trang
     */
    Page<Order> findByStatusNotAndDeliveryType(OrderStatus status, DeliveryType deliveryType, Pageable pageable);

    /**
     * Lấy mã đơn hàng cuối cùng theo ngày
     *
     * @param datePattern pattern ngày tháng (yyyyMMdd)
     * @return danh sách mã đơn hàng
     */
    @Query("SELECT o.orderCode FROM Order o WHERE o.orderCode LIKE CONCAT('ORD', :datePattern, '%') ORDER BY o.orderCode DESC")
    Page<String> findLastOrderCodeByDate(@Param("datePattern") String datePattern, Pageable pageable);

    /**
     * Lấy danh sách đơn hàng của khách hàng theo customerId với phân trang
     *
     * @param customerId ID khách hàng
     * @param pageable thông tin phân trang và sắp xếp
     * @return danh sách đơn hàng có phân trang
     */
    Page<Order> findByCustomerCustomerId(Integer customerId, Pageable pageable);

    /**
     * Đếm số lượng đơn hàng trong khoảng thời gian (dashboard)
     *
     * @param fromDate từ ngày
     * @param toDate   đến ngày
     * @return số lượng đơn hàng
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE " +
                    "CAST(o.orderDate AS DATE) >= :fromDate " +
                    "AND CAST(o.orderDate AS DATE) <= :toDate")
    long countOrdersByDateRange(
                    @Param("fromDate") java.time.LocalDate fromDate,
                    @Param("toDate") java.time.LocalDate toDate);

    /**
     * Tính tổng giá trị đơn hàng trong khoảng thời gian (dashboard)
     *
     * @param fromDate từ ngày
     * @param toDate   đến ngày
     * @return tổng giá trị đơn hàng
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE " +
                    "CAST(o.orderDate AS DATE) >= :fromDate " +
                    "AND CAST(o.orderDate AS DATE) <= :toDate")
    java.math.BigDecimal sumOrdersTotalByDateRange(
                    @Param("fromDate") java.time.LocalDate fromDate,
                    @Param("toDate") java.time.LocalDate toDate);
}
