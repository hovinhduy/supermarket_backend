package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.ProductUnitImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho ProductUnitImage entity
 * Cung cấp các phương thức truy vấn cho mối quan hệ giữa ProductUnit và ProductImage
 */
@Repository
public interface ProductUnitImageRepository extends JpaRepository<ProductUnitImage, Long> {

    /**
     * Tìm tất cả hình ảnh được chọn cho một ProductUnit cụ thể
     * @param productUnitId ID của ProductUnit
     * @return Danh sách ProductUnitImage được sắp xếp theo displayOrder
     */
    @Query("SELECT pui FROM ProductUnitImage pui " +
           "WHERE pui.productUnit.id = :productUnitId " +
           "AND pui.isActive = true " +
           "ORDER BY pui.displayOrder ASC, pui.createdAt ASC")
    List<ProductUnitImage> findByProductUnitIdOrderByDisplayOrder(@Param("productUnitId") Long productUnitId);

    /**
     * Tìm hình ảnh chính của một ProductUnit
     * @param productUnitId ID của ProductUnit
     * @return ProductUnitImage được đánh dấu là primary
     */
    @Query("SELECT pui FROM ProductUnitImage pui " +
           "WHERE pui.productUnit.id = :productUnitId " +
           "AND pui.isPrimary = true " +
           "AND pui.isActive = true")
    Optional<ProductUnitImage> findPrimaryImageByProductUnitId(@Param("productUnitId") Long productUnitId);

    /**
     * Tìm tất cả ProductUnit sử dụng một hình ảnh cụ thể
     * @param productImageId ID của ProductImage
     * @return Danh sách ProductUnitImage
     */
    @Query("SELECT pui FROM ProductUnitImage pui " +
           "WHERE pui.productImage.imageId = :productImageId " +
           "AND pui.isActive = true")
    List<ProductUnitImage> findByProductImageId(@Param("productImageId") Integer productImageId);

    /**
     * Kiểm tra xem một ProductUnit đã có hình ảnh chính chưa
     * @param productUnitId ID của ProductUnit
     * @return true nếu đã có hình ảnh chính
     */
    @Query("SELECT COUNT(pui) > 0 FROM ProductUnitImage pui " +
           "WHERE pui.productUnit.id = :productUnitId " +
           "AND pui.isPrimary = true " +
           "AND pui.isActive = true")
    boolean existsPrimaryImageForProductUnit(@Param("productUnitId") Long productUnitId);

    /**
     * Tìm mapping cụ thể giữa ProductUnit và ProductImage
     * @param productUnitId ID của ProductUnit
     * @param productImageId ID của ProductImage
     * @return ProductUnitImage nếu tồn tại
     */
    @Query("SELECT pui FROM ProductUnitImage pui " +
           "WHERE pui.productUnit.id = :productUnitId " +
           "AND pui.productImage.imageId = :productImageId " +
           "AND pui.isActive = true")
    Optional<ProductUnitImage> findByProductUnitIdAndProductImageId(
            @Param("productUnitId") Long productUnitId, 
            @Param("productImageId") Integer productImageId);

    /**
     * Đếm số lượng hình ảnh được chọn cho một ProductUnit
     * @param productUnitId ID của ProductUnit
     * @return Số lượng hình ảnh
     */
    @Query("SELECT COUNT(pui) FROM ProductUnitImage pui " +
           "WHERE pui.productUnit.id = :productUnitId " +
           "AND pui.isActive = true")
    long countByProductUnitId(@Param("productUnitId") Long productUnitId);

    /**
     * Tìm tất cả hình ảnh của các ProductUnit thuộc về một Product cụ thể
     * @param productId ID của Product
     * @return Danh sách ProductUnitImage
     */
    @Query("SELECT pui FROM ProductUnitImage pui " +
           "WHERE pui.productUnit.product.id = :productId " +
           "AND pui.isActive = true " +
           "ORDER BY pui.productUnit.id, pui.displayOrder ASC")
    List<ProductUnitImage> findByProductId(@Param("productId") Long productId);

    /**
     * Xóa tất cả mapping của một ProductUnit (soft delete)
     * @param productUnitId ID của ProductUnit
     */
    @Query("UPDATE ProductUnitImage pui SET pui.isActive = false " +
           "WHERE pui.productUnit.id = :productUnitId")
    void deactivateByProductUnitId(@Param("productUnitId") Long productUnitId);

    /**
     * Xóa tất cả mapping sử dụng một ProductImage cụ thể (soft delete)
     * @param productImageId ID của ProductImage
     */
    @Query("UPDATE ProductUnitImage pui SET pui.isActive = false " +
           "WHERE pui.productImage.imageId = :productImageId")
    void deactivateByProductImageId(@Param("productImageId") Integer productImageId);
}
