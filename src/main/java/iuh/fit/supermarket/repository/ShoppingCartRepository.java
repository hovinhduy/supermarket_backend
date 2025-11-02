package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository để truy vấn dữ liệu giỏ hàng
 */
@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Integer> {

    /**
     * Tìm giỏ hàng theo customer ID
     *
     * @param customerId ID khách hàng
     * @return Optional chứa giỏ hàng nếu tìm thấy
     */
    @Query("SELECT sc FROM ShoppingCart sc WHERE sc.customer.customerId = :customerId")
    Optional<ShoppingCart> findByCustomerId(@Param("customerId") Integer customerId);

    /**
     * Kiểm tra xem customer đã có giỏ hàng chưa
     *
     * @param customerId ID khách hàng
     * @return true nếu có giỏ hàng
     */
    @Query("SELECT CASE WHEN COUNT(sc) > 0 THEN true ELSE false END FROM ShoppingCart sc WHERE sc.customer.customerId = :customerId")
    boolean existsByCustomerId(@Param("customerId") Integer customerId);
}
