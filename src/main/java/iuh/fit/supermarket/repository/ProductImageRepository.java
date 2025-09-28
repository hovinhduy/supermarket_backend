package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho thao tác với dữ liệu hình ảnh sản phẩm
 */
@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {

    /**
     * Tìm tất cả hình ảnh của một sản phẩm, sắp xếp theo thứ tự
     *
     * @param productId ID của sản phẩm
     * @return danh sách hình ảnh của sản phẩm
     */
    List<ProductImage> findByProduct_IdOrderBySortOrderAscCreatedAtAsc(Long productId);

    /**
     * Tìm hình ảnh chính của sản phẩm (sortOrder = 0 hoặc thấp nhất)
     *
     * @param productId ID của sản phẩm
     * @return hình ảnh chính của sản phẩm
     */
    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id = :productId ORDER BY pi.sortOrder ASC, pi.createdAt ASC")
    Optional<ProductImage> findMainImageByProductId(@Param("productId") Long productId);

    /**
     * Tìm hình ảnh theo URL
     *
     * @param imageUrl URL của hình ảnh
     * @return hình ảnh tương ứng
     */
    Optional<ProductImage> findByImageUrl(String imageUrl);

    /**
     * Đếm số lượng hình ảnh của một sản phẩm
     *
     * @param productId ID của sản phẩm
     * @return số lượng hình ảnh
     */
    @Query("SELECT COUNT(pi) FROM ProductImage pi WHERE pi.product.id = :productId")
    long countByProductId(@Param("productId") Long productId);

    /**
     * Xóa tất cả hình ảnh của một sản phẩm
     *
     * @param productId ID của sản phẩm
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ProductImage pi WHERE pi.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);

    /**
     * Tìm hình ảnh chung của sản phẩm
     *
     * @param productId ID của sản phẩm
     * @return danh sách hình ảnh chung của sản phẩm
     */
    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id = :productId ORDER BY pi.sortOrder ASC, pi.createdAt ASC")
    List<ProductImage> findGeneralImagesByProductId(@Param("productId") Long productId);

    /**
     * Tìm thứ tự sắp xếp lớn nhất cho sản phẩm
     *
     * @param productId ID của sản phẩm
     * @return thứ tự sắp xếp lớn nhất
     */
    @Query("SELECT COALESCE(MAX(pi.sortOrder), 0) FROM ProductImage pi WHERE pi.product.id = :productId")
    Integer findMaxSortOrderByProductId(@Param("productId") Long productId);

    /**
     * Tìm tất cả hình ảnh của sản phẩm mà chưa được chọn bởi ProductUnit cụ thể
     *
     * @param productId ID của sản phẩm
     * @param productUnitId ID của ProductUnit
     * @return danh sách hình ảnh chưa được chọn
     */
    @Query("SELECT pi FROM ProductImage pi " +
           "WHERE pi.product.id = :productId " +
           "AND pi.imageId NOT IN (" +
           "    SELECT pui.productImage.imageId FROM ProductUnitImage pui " +
           "    WHERE pui.productUnit.id = :productUnitId AND pui.isActive = true" +
           ") " +
           "ORDER BY pi.sortOrder ASC, pi.createdAt ASC")
    List<ProductImage> findAvailableImagesForProductUnit(@Param("productId") Long productId,
                                                        @Param("productUnitId") Long productUnitId);

    /**
     * Tìm tất cả hình ảnh của sản phẩm đã được chọn bởi ProductUnit cụ thể
     *
     * @param productId ID của sản phẩm
     * @param productUnitId ID của ProductUnit
     * @return danh sách hình ảnh đã được chọn
     */
    @Query("SELECT pi FROM ProductImage pi " +
           "JOIN ProductUnitImage pui ON pi.imageId = pui.productImage.imageId " +
           "WHERE pi.product.id = :productId " +
           "AND pui.productUnit.id = :productUnitId " +
           "AND pui.isActive = true " +
           "ORDER BY pui.displayOrder ASC, pi.sortOrder ASC")
    List<ProductImage> findSelectedImagesForProductUnit(@Param("productId") Long productId,
                                                       @Param("productUnitId") Long productUnitId);
}
