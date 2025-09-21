package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho quản lý tồn kho
 */
@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Integer> {

    /**
     * Tìm tồn kho theo biến thể sản phẩm
     * 
     * @param variantId ID biến thể sản phẩm
     * @return Optional<Warehouse>
     */
    @Query("SELECT w FROM Warehouse w WHERE w.variant.variantId = :variantId")
    Optional<Warehouse> findByVariantId(@Param("variantId") Long variantId);

    /**
     * Kiểm tra tồn tại tồn kho theo biến thể sản phẩm
     * 
     * @param variantId ID biến thể sản phẩm
     * @return true nếu tồn tại
     */
    @Query("SELECT COUNT(w) > 0 FROM Warehouse w WHERE w.variant.variantId = :variantId")
    boolean existsByVariantId(@Param("variantId") Long variantId);

    /**
     * Lấy danh sách tồn kho có số lượng thấp (dưới ngưỡng cảnh báo)
     * 
     * @param minQuantity ngưỡng số lượng tối thiểu
     * @return List<Warehouse>
     */
    @Query("SELECT w FROM Warehouse w WHERE w.quantityOnHand <= :minQuantity ORDER BY w.quantityOnHand ASC")
    List<Warehouse> findLowStockWarehouses(@Param("minQuantity") Integer minQuantity);

    /**
     * Lấy danh sách tồn kho có số lượng thấp với phân trang
     * 
     * @param minQuantity ngưỡng số lượng tối thiểu
     * @param pageable thông tin phân trang
     * @return Page<Warehouse>
     */
    @Query("SELECT w FROM Warehouse w WHERE w.quantityOnHand <= :minQuantity ORDER BY w.quantityOnHand ASC")
    Page<Warehouse> findLowStockWarehouses(@Param("minQuantity") Integer minQuantity, Pageable pageable);

    /**
     * Lấy danh sách tồn kho hết hàng (số lượng = 0)
     * 
     * @return List<Warehouse>
     */
    @Query("SELECT w FROM Warehouse w WHERE w.quantityOnHand = 0")
    List<Warehouse> findOutOfStockWarehouses();

    /**
     * Lấy danh sách tồn kho hết hàng với phân trang
     * 
     * @param pageable thông tin phân trang
     * @return Page<Warehouse>
     */
    @Query("SELECT w FROM Warehouse w WHERE w.quantityOnHand = 0")
    Page<Warehouse> findOutOfStockWarehouses(Pageable pageable);

    /**
     * Lấy danh sách tồn kho theo sản phẩm (tất cả biến thể)
     * 
     * @param productId ID sản phẩm
     * @return List<Warehouse>
     */
    @Query("SELECT w FROM Warehouse w WHERE w.variant.product.id = :productId ORDER BY w.variant.variantName")
    List<Warehouse> findByProductId(@Param("productId") Long productId);

    /**
     * Tính tổng số lượng tồn kho theo sản phẩm (tất cả biến thể)
     * 
     * @param productId ID sản phẩm
     * @return tổng số lượng tồn kho
     */
    @Query("SELECT COALESCE(SUM(w.quantityOnHand), 0) FROM Warehouse w WHERE w.variant.product.id = :productId")
    Integer sumQuantityByProductId(@Param("productId") Long productId);

    /**
     * Lấy danh sách tồn kho với thông tin biến thể (để tránh N+1 query)
     * 
     * @param pageable thông tin phân trang
     * @return Page<Warehouse>
     */
    @Query("SELECT w FROM Warehouse w " +
           "LEFT JOIN FETCH w.variant v " +
           "LEFT JOIN FETCH v.product p " +
           "LEFT JOIN FETCH v.unit u " +
           "ORDER BY w.updatedAt DESC")
    Page<Warehouse> findAllWithVariantDetails(Pageable pageable);

    /**
     * Tìm kiếm tồn kho theo từ khóa (tên sản phẩm, mã biến thể, mã vạch)
     * 
     * @param keyword từ khóa tìm kiếm
     * @return List<Warehouse>
     */
    @Query("SELECT w FROM Warehouse w WHERE " +
           "LOWER(w.variant.variantName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.variant.variantCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.variant.barcode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.variant.product.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Warehouse> findByKeyword(@Param("keyword") String keyword);

    /**
     * Tìm kiếm tồn kho theo từ khóa với phân trang
     * 
     * @param keyword từ khóa tìm kiếm
     * @param pageable thông tin phân trang
     * @return Page<Warehouse>
     */
    @Query("SELECT w FROM Warehouse w WHERE " +
           "LOWER(w.variant.variantName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.variant.variantCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.variant.barcode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.variant.product.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Warehouse> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Đếm tổng số loại sản phẩm có tồn kho
     * 
     * @return số lượng loại sản phẩm
     */
    @Query("SELECT COUNT(w) FROM Warehouse w WHERE w.quantityOnHand > 0")
    Long countProductsInStock();

    /**
     * Tính tổng giá trị tồn kho (cần join với bảng giá)
     * Lưu ý: Query này cần được điều chỉnh khi có bảng giá
     * 
     * @return tổng số lượng tồn kho
     */
    @Query("SELECT COALESCE(SUM(w.quantityOnHand), 0) FROM Warehouse w")
    Long sumTotalQuantityOnHand();
}
