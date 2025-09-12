package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.ProductUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho thao tác với dữ liệu đơn vị sản phẩm
 */
@Repository
public interface ProductUnitRepository extends JpaRepository<ProductUnit, Long> {

    /**
     * Tìm đơn vị theo productId
     */
    List<ProductUnit> findByProductId(Long productId);

    /**
     * Tìm đơn vị theo mã code
     */
    Optional<ProductUnit> findByCode(String code);

    // Method findByBarcode đã được chuyển sang
    // ProductVariantRepository.findByBarcode()
    // vì barcode giờ nằm trong ProductVariant entity

    /**
     * Kiểm tra sự tồn tại của mã đơn vị
     */
    boolean existsByCode(String code);

    /**
     * Tìm đơn vị theo productId và tên đơn vị
     */
    Optional<ProductUnit> findByProductIdAndUnit(Long productId, String unit);

    /**
     * Lấy danh sách đơn vị hoạt động theo productId
     * (Logic "cho phép bán" đã được chuyển sang ProductVariant.allowsSale)
     */
    @Query("SELECT pu FROM ProductUnit pu WHERE pu.product.id = :productId AND pu.isActive = true ORDER BY pu.sortOrder ASC")
    List<ProductUnit> findActiveUnitsByProductId(@Param("productId") Long productId);

    /**
     * Xóa tất cả đơn vị theo productId
     */
    void deleteByProductId(Long productId);

    /**
     * Tìm đơn vị theo productId và cờ isBaseUnit
     */
    Optional<ProductUnit> findByProductIdAndIsBaseUnit(Long productId, Boolean isBaseUnit);
}
