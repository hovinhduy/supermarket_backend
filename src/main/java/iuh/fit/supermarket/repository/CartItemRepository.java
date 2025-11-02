package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.CartItem;
import iuh.fit.supermarket.entity.CartItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository để truy vấn dữ liệu cart item
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, CartItemId> {

    /**
     * Lấy tất cả items trong một giỏ hàng
     *
     * @param cartId ID giỏ hàng
     * @return Danh sách cart items
     */
    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.productUnit pu JOIN FETCH pu.product p WHERE ci.cart.cartId = :cartId")
    List<CartItem> findByCartId(@Param("cartId") Integer cartId);

    /**
     * Tìm cart item theo cart ID và product unit ID
     *
     * @param cartId        ID giỏ hàng
     * @param productUnitId ID product unit
     * @return Optional chứa cart item nếu tìm thấy
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.cartId = :cartId AND ci.productUnit.id = :productUnitId")
    Optional<CartItem> findByCartIdAndProductUnitId(@Param("cartId") Integer cartId, @Param("productUnitId") Long productUnitId);

    /**
     * Xóa tất cả items trong giỏ hàng
     *
     * @param cartId ID giỏ hàng
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    void deleteByCartId(@Param("cartId") Integer cartId);

    /**
     * Xóa một item cụ thể trong giỏ hàng
     *
     * @param cartId        ID giỏ hàng
     * @param productUnitId ID product unit
     */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.cartId = :cartId AND ci.productUnit.id = :productUnitId")
    void deleteByCartIdAndProductUnitId(@Param("cartId") Integer cartId, @Param("productUnitId") Long productUnitId);

    /**
     * Đếm số lượng items trong giỏ hàng
     *
     * @param cartId ID giỏ hàng
     * @return Số lượng items
     */
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    Integer countByCartId(@Param("cartId") Integer cartId);
}
