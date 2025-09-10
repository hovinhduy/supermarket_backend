package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.ProductVariant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository để xử lý các thao tác CRUD với ProductVariant
 */
@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    /**
     * Tìm tất cả biến thể của một sản phẩm
     * 
     * @param productId ID của sản phẩm
     * @return danh sách biến thể của sản phẩm
     */
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId AND pv.isDeleted = false")
    List<ProductVariant> findByProductId(@Param("productId") Long productId);

    /**
     * Tìm tất cả biến thể của một sản phẩm (bao gồm cả đã xóa)
     * 
     * @param productId ID của sản phẩm
     * @return danh sách tất cả biến thể của sản phẩm
     */
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId")
    List<ProductVariant> findAllByProductId(@Param("productId") Long productId);

    /**
     * Tìm biến thể theo mã biến thể
     * 
     * @param variantCode mã biến thể
     * @return biến thể nếu tìm thấy
     */
    Optional<ProductVariant> findByVariantCode(String variantCode);

    /**
     * Tìm biến thể theo mã vạch
     * 
     * @param barcode mã vạch
     * @return biến thể nếu tìm thấy
     */
    Optional<ProductVariant> findByBarcode(String barcode);

    /**
     * Tìm biến thể theo sản phẩm và đơn vị
     * 
     * @param productId ID của sản phẩm
     * @param unitId    ID của đơn vị
     * @return biến thể nếu tìm thấy
     */
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId AND pv.unit.id = :unitId AND pv.isDeleted = false")
    Optional<ProductVariant> findByProductIdAndUnitId(@Param("productId") Long productId, @Param("unitId") Long unitId);

    /**
     * Tìm tất cả biến thể hoạt động của một sản phẩm
     * 
     * @param productId ID của sản phẩm
     * @return danh sách biến thể hoạt động
     */
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId AND pv.isActive = true AND pv.isDeleted = false")
    List<ProductVariant> findActiveByProductId(@Param("productId") Long productId);

    /**
     * Tìm tất cả biến thể có thể bán của một sản phẩm
     * 
     * @param productId ID của sản phẩm
     * @return danh sách biến thể có thể bán
     */
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.product.id = :productId AND pv.allowsSale = true AND pv.isActive = true AND pv.isDeleted = false")
    List<ProductVariant> findSaleableByProductId(@Param("productId") Long productId);

    /**
     * Tìm biến thể có tồn kho thấp (dưới mức tối thiểu)
     * 
     * @return danh sách biến thể cần bổ sung
     */
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.quantityOnHand <= pv.minQuantity AND pv.isActive = true AND pv.isDeleted = false")
    List<ProductVariant> findLowStockVariants();

    /**
     * Tìm biến thể theo từ khóa (tên, mã)
     * 
     * @param keyword  từ khóa tìm kiếm
     * @param pageable thông tin phân trang
     * @return danh sách biến thể phân trang
     */
    @Query("SELECT pv FROM ProductVariant pv WHERE (pv.variantName LIKE %:keyword% OR pv.variantCode LIKE %:keyword%) AND pv.isDeleted = false")
    Page<ProductVariant> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Tìm mã variant lớn nhất có định dạng SP + 6 chữ số
     */
    @Query("SELECT pv.variantCode FROM ProductVariant pv WHERE pv.variantCode LIKE 'SP%' ORDER BY pv.variantCode DESC")
    List<String> findMaxVariantCode();

    /**
     * Đếm số lượng biến thể của một sản phẩm
     * 
     * @param productId ID của sản phẩm
     * @return số lượng biến thể
     */
    @Query("SELECT COUNT(pv) FROM ProductVariant pv WHERE pv.product.id = :productId AND pv.isDeleted = false")
    long countByProductId(@Param("productId") Long productId);

    /**
     * Kiểm tra mã biến thể đã tồn tại hay chưa
     * 
     * @param variantCode mã biến thể
     * @return true nếu đã tồn tại
     */
    boolean existsByVariantCode(String variantCode);

    /**
     * Kiểm tra mã vạch đã tồn tại hay chưa
     * 
     * @param barcode mã vạch
     * @return true nếu đã tồn tại
     */
    boolean existsByBarcode(String barcode);

    /**
     * Cập nhật số lượng tồn kho của biến thể
     * 
     * @param variantId   ID của biến thể
     * @param newQuantity số lượng mới
     */
    @Query("UPDATE ProductVariant pv SET pv.quantityOnHand = :newQuantity WHERE pv.variantId = :variantId")
    void updateQuantityOnHand(@Param("variantId") Long variantId, @Param("newQuantity") BigDecimal newQuantity);

    /**
     * Cập nhật số lượng đặt trước của biến thể
     * 
     * @param variantId   ID của biến thể
     * @param newReserved số lượng đặt trước mới
     */
    @Query("UPDATE ProductVariant pv SET pv.quantityReserved = :newReserved WHERE pv.variantId = :variantId")
    void updateQuantityReserved(@Param("variantId") Long variantId, @Param("newReserved") BigDecimal newReserved);

    /**
     * Tìm biến thể theo giá bán trong khoảng
     * 
     * @param minPrice giá tối thiểu
     * @param maxPrice giá tối đa
     * @return danh sách biến thể trong khoảng giá
     */
    @Query("SELECT pv FROM ProductVariant pv WHERE pv.basePrice BETWEEN :minPrice AND :maxPrice AND pv.isDeleted = false")
    List<ProductVariant> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);
}
