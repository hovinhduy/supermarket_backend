package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.dto.favorite.CustomerFavoriteResponse;
import iuh.fit.supermarket.entity.CustomerFavorite;
import iuh.fit.supermarket.entity.CustomerFavoriteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository để truy vấn dữ liệu sản phẩm yêu thích của khách hàng
 */
@Repository
public interface CustomerFavoriteRepository extends JpaRepository<CustomerFavorite, CustomerFavoriteId> {

    /**
     * Kiểm tra xem sản phẩm đã có trong danh sách yêu thích chưa
     *
     * @param customerId    ID khách hàng
     * @param productUnitId ID đơn vị sản phẩm
     * @return true nếu đã tồn tại
     */
    @Query("SELECT CASE WHEN COUNT(cf) > 0 THEN true ELSE false END " +
            "FROM CustomerFavorite cf " +
            "WHERE cf.customer.customerId = :customerId AND cf.productUnit.id = :productUnitId")
    boolean existsByCustomerIdAndProductUnitId(@Param("customerId") Integer customerId,
                                                @Param("productUnitId") Long productUnitId);

    /**
     * Tìm một sản phẩm yêu thích cụ thể
     *
     * @param customerId    ID khách hàng
     * @param productUnitId ID đơn vị sản phẩm
     * @return Optional chứa CustomerFavorite nếu tìm thấy
     */
    @Query("SELECT cf FROM CustomerFavorite cf " +
            "WHERE cf.customer.customerId = :customerId AND cf.productUnit.id = :productUnitId")
    Optional<CustomerFavorite> findByCustomerIdAndProductUnitId(@Param("customerId") Integer customerId,
                                                                  @Param("productUnitId") Long productUnitId);

    /**
     * Lấy danh sách sản phẩm yêu thích của khách hàng với thông tin chi tiết
     *
     * @param customerId ID khách hàng
     * @return Danh sách CustomerFavoriteResponse
     */
    @Query("SELECT new iuh.fit.supermarket.dto.favorite.CustomerFavoriteResponse(" +
            "pu.id, " +
            "p.code, " +
            "p.name, " +
            "u.name, " +
            "CAST(COALESCE(pd.salePrice, 0) AS double), " +
            "null, " +
            "true, " +
            "cf.createdAt) " +
            "FROM CustomerFavorite cf " +
            "JOIN cf.productUnit pu " +
            "JOIN pu.product p " +
            "JOIN pu.unit u " +
            "LEFT JOIN PriceDetail pd ON pd.productUnit.id = pu.id " +
            "  AND pd.price.status = iuh.fit.supermarket.enums.PriceType.ACTIVE " +
            "  AND pd.price.startDate <= CURRENT_TIMESTAMP " +
            "  AND pd.price.endDate > CURRENT_TIMESTAMP " +
            "WHERE cf.customer.customerId = :customerId " +
            "ORDER BY cf.createdAt DESC")
    List<CustomerFavoriteResponse> findFavoritesByCustomerId(@Param("customerId") Integer customerId);

    /**
     * Xóa sản phẩm khỏi danh sách yêu thích
     *
     * @param customerId    ID khách hàng
     * @param productUnitId ID đơn vị sản phẩm
     */
    @Modifying
    @Query("DELETE FROM CustomerFavorite cf " +
            "WHERE cf.customer.customerId = :customerId AND cf.productUnit.id = :productUnitId")
    void deleteByCustomerIdAndProductUnitId(@Param("customerId") Integer customerId,
                                             @Param("productUnitId") Long productUnitId);

    /**
     * Đếm số lượng sản phẩm yêu thích của khách hàng
     *
     * @param customerId ID khách hàng
     * @return Số lượng sản phẩm yêu thích
     */
    @Query("SELECT COUNT(cf) FROM CustomerFavorite cf WHERE cf.customer.customerId = :customerId")
    Integer countByCustomerId(@Param("customerId") Integer customerId);
}
