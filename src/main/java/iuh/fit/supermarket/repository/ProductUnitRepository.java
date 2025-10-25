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
 * Quản lý mối quan hệ giữa sản phẩm và đơn vị tính
 */
@Repository
public interface ProductUnitRepository extends JpaRepository<ProductUnit, Long> {

    /**
     * Tìm tất cả đơn vị sản phẩm theo productId
     *
     * @param productId ID sản phẩm
     * @return danh sách đơn vị sản phẩm
     */
    List<ProductUnit> findByProductId(Long productId);

    /**
     * Tìm đơn vị sản phẩm theo barcode
     *
     * @param barcode mã vạch
     * @return đơn vị sản phẩm nếu tìm thấy
     */
    Optional<ProductUnit> findByBarcode(String barcode);

    /**
     * Kiểm tra sự tồn tại của barcode
     *
     * @param barcode mã vạch
     * @return true nếu tồn tại, false nếu không
     */
    boolean existsByBarcode(String barcode);

    /**
     * Tìm đơn vị sản phẩm theo productId và unitId
     *
     * @param productId ID sản phẩm
     * @param unitId    ID đơn vị tính
     * @return đơn vị sản phẩm nếu tìm thấy
     */
    Optional<ProductUnit> findByProductIdAndUnitId(Long productId, Long unitId);

    /**
     * Xóa tất cả đơn vị sản phẩm theo productId (soft delete)
     *
     * @param productId ID sản phẩm
     */
    @Query("UPDATE ProductUnit pu SET pu.isDeleted = true WHERE pu.product.id = :productId")
    void softDeleteByProductId(@Param("productId") Long productId);

    /**
     * Tìm đơn vị cơ bản của sản phẩm
     *
     * @param productId ID sản phẩm
     * @return đơn vị cơ bản nếu tìm thấy
     */
    Optional<ProductUnit> findByProductIdAndIsBaseUnit(Long productId, Boolean isBaseUnit);

    /**
     * Tìm đơn vị sản phẩm theo productId và unitId với cờ isBaseUnit
     *
     * @param productId  ID sản phẩm
     * @param unitId     ID đơn vị tính
     * @param isBaseUnit cờ đơn vị cơ bản
     * @return đơn vị sản phẩm nếu tìm thấy
     */
    Optional<ProductUnit> findByProductIdAndUnitIdAndIsBaseUnit(Long productId, Long unitId, Boolean isBaseUnit);

    /**
     * Đếm số lượng đơn vị sản phẩm theo productId
     *
     * @param productId ID sản phẩm
     * @return số lượng đơn vị sản phẩm
     */
    @Query("SELECT COUNT(pu) FROM ProductUnit pu WHERE pu.product.id = :productId AND pu.isDeleted = false")
    Long countByProductId(@Param("productId") Long productId);

    /**
     * Kiểm tra sản phẩm có đơn vị cơ bản không
     *
     * @param productId ID sản phẩm
     * @return true nếu có đơn vị cơ bản, false nếu không
     */
    @Query("SELECT COUNT(pu) > 0 FROM ProductUnit pu WHERE pu.product.id = :productId AND pu.isBaseUnit = true AND pu.isDeleted = false")
    boolean hasBaseUnit(@Param("productId") Long productId);

    /**
     * Tìm tất cả đơn vị sản phẩm sử dụng một đơn vị tính cụ thể
     *
     * @param unitId ID đơn vị tính
     * @return danh sách đơn vị sản phẩm
     */
    @Query("SELECT pu FROM ProductUnit pu WHERE pu.unit.id = :unitId AND pu.isDeleted = false")
    List<ProductUnit> findByUnitId(@Param("unitId") Long unitId);

    /**
     * Tìm đơn vị sản phẩm theo ID và productId
     *
     * @param id        ID đơn vị sản phẩm
     * @param productId ID sản phẩm
     * @return đơn vị sản phẩm nếu tìm thấy
     */
    @Query("SELECT pu FROM ProductUnit pu WHERE pu.id = :id AND pu.product.id = :productId AND pu.isDeleted = false")
    Optional<ProductUnit> findByIdAndProductId(@Param("id") Long id, @Param("productId") Long productId);

    /**
     * Tìm tất cả đơn vị sản phẩm hoạt động theo productId
     *
     * @param productId ID sản phẩm
     * @return danh sách đơn vị sản phẩm hoạt động
     */
    @Query("SELECT pu FROM ProductUnit pu WHERE pu.product.id = :productId AND pu.isDeleted = false AND pu.isActive = true")
    List<ProductUnit> findActiveByProductId(@Param("productId") Long productId);

    /**
     * Kiểm tra tồn tại barcode trùng (trừ bản thân)
     *
     * @param barcode   mã barcode cần kiểm tra
     * @param excludeId ID cần loại trừ
     * @return true nếu tồn tại, false nếu không
     */
    boolean existsByBarcodeAndIdNot(String barcode, Long excludeId);

    /**
     * Tìm kiếm ProductUnit theo tên sản phẩm, mã sản phẩm hoặc barcode
     *
     * @param searchTerm từ khóa tìm kiếm
     * @return danh sách ProductUnit tìm được
     */
    @Query("SELECT DISTINCT pu FROM ProductUnit pu " +
            "LEFT JOIN FETCH pu.product p " +
            "LEFT JOIN FETCH pu.unit u " +
            "LEFT JOIN FETCH pu.productUnitImages pui " +
            "LEFT JOIN FETCH pui.productImage pi " +
            "WHERE pu.isDeleted = false " +
            "AND pu.isActive = true " +
            "AND p.isDeleted = false " +
            "AND p.isActive = true " +
            "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(p.code) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(pu.barcode) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<ProductUnit> searchProductUnits(@Param("searchTerm") String searchTerm);

}
