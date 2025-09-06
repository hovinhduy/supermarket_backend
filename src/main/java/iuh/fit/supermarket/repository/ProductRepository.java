package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho thao tác với dữ liệu sản phẩm
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Tìm sản phẩm theo mã code
     */
    Optional<Product> findByCode(String code);

    /**
     * Tìm sản phẩm theo mã vạch
     */
    Optional<Product> findByBarcode(String barcode);

    /**
     * Lấy danh sách sản phẩm theo trạng thái hoạt động
     */
    List<Product> findByIsActiveAndIsDeleted(Boolean isActive, Boolean isDeleted);

    /**
     * Lấy danh sách sản phẩm theo danh mục
     */
    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId AND p.isDeleted = :isDeleted")
    List<Product> findByCategoryIdAndIsDeleted(@Param("categoryId") Long categoryId,
            @Param("isDeleted") Boolean isDeleted);

    /**
     * Kiểm tra sự tồn tại của mã sản phẩm
     */
    boolean existsByCode(String code);

    /**
     * Tìm mã sản phẩm lớn nhất có định dạng SP + 6 chữ số
     */
    @Query("SELECT p.code FROM Product p WHERE p.code LIKE 'SP%' ORDER BY p.code DESC")
    List<String> findMaxProductCode();

    /**
     * Tìm sản phẩm theo tên chứa từ khóa
     */
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:keyword% AND p.isDeleted = false")
    List<Product> findByNameContaining(@Param("keyword") String keyword);

    /**
     * Lấy danh sách sản phẩm có tồn kho thấp
     */
    @Query("SELECT p FROM Product p WHERE p.onHand <= p.minQuantity AND p.isDeleted = false")
    List<Product> findLowStockProducts();

    /**
     * Lấy danh sách sản phẩm có biến thể
     */
    List<Product> findByHasVariantsAndIsDeleted(Boolean hasVariants, Boolean isDeleted);
}
