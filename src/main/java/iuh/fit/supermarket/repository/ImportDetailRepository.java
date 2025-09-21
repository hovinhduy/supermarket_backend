package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.ImportDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface cho quản lý chi tiết phiếu nhập hàng
 */
@Repository
public interface ImportDetailRepository extends JpaRepository<ImportDetail, Integer> {

    /**
     * Lấy danh sách chi tiết theo phiếu nhập
     * 
     * @param importId ID phiếu nhập
     * @return List<ImportDetail>
     */
    @Query("SELECT id FROM ImportDetail id WHERE id.importRecord.importId = :importId ORDER BY id.createdAt")
    List<ImportDetail> findByImportId(@Param("importId") Integer importId);

    /**
     * Lấy danh sách chi tiết theo biến thể sản phẩm
     * 
     * @param variantId ID biến thể sản phẩm
     * @return List<ImportDetail>
     */
    @Query("SELECT id FROM ImportDetail id WHERE id.variant.variantId = :variantId ORDER BY id.createdAt DESC")
    List<ImportDetail> findByVariantId(@Param("variantId") Long variantId);

    /**
     * Lấy danh sách chi tiết theo biến thể sản phẩm trong khoảng thời gian
     * 
     * @param variantId ID biến thể sản phẩm
     * @param startDate ngày bắt đầu
     * @param endDate ngày kết thúc
     * @return List<ImportDetail>
     */
    @Query("SELECT id FROM ImportDetail id WHERE id.variant.variantId = :variantId " +
           "AND id.createdAt BETWEEN :startDate AND :endDate ORDER BY id.createdAt DESC")
    List<ImportDetail> findByVariantIdAndDateRange(@Param("variantId") Long variantId,
                                                   @Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Tính tổng số lượng nhập theo biến thể sản phẩm
     * 
     * @param variantId ID biến thể sản phẩm
     * @return tổng số lượng nhập
     */
    @Query("SELECT COALESCE(SUM(id.quantity), 0) FROM ImportDetail id WHERE id.variant.variantId = :variantId")
    Integer sumQuantityByVariantId(@Param("variantId") Long variantId);

    /**
     * Tính tổng số lượng nhập theo biến thể sản phẩm trong khoảng thời gian
     * 
     * @param variantId ID biến thể sản phẩm
     * @param startDate ngày bắt đầu
     * @param endDate ngày kết thúc
     * @return tổng số lượng nhập
     */
    @Query("SELECT COALESCE(SUM(id.quantity), 0) FROM ImportDetail id WHERE id.variant.variantId = :variantId " +
           "AND id.createdAt BETWEEN :startDate AND :endDate")
    Integer sumQuantityByVariantIdAndDateRange(@Param("variantId") Long variantId,
                                              @Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * Tính tổng số lượng nhập theo phiếu nhập
     * 
     * @param importId ID phiếu nhập
     * @return tổng số lượng nhập
     */
    @Query("SELECT COALESCE(SUM(id.quantity), 0) FROM ImportDetail id WHERE id.importRecord.importId = :importId")
    Integer sumQuantityByImportId(@Param("importId") Integer importId);

    /**
     * Đếm số lượng loại sản phẩm khác nhau trong phiếu nhập
     * 
     * @param importId ID phiếu nhập
     * @return số lượng loại sản phẩm
     */
    @Query("SELECT COUNT(DISTINCT id.variant.variantId) FROM ImportDetail id WHERE id.importRecord.importId = :importId")
    Integer countDistinctVariantsByImportId(@Param("importId") Integer importId);

    /**
     * Lấy danh sách chi tiết nhập hàng với thông tin biến thể (để tránh N+1 query)
     * 
     * @param importId ID phiếu nhập
     * @return List<ImportDetail>
     */
    @Query("SELECT id FROM ImportDetail id " +
           "LEFT JOIN FETCH id.variant v " +
           "LEFT JOIN FETCH v.product p " +
           "LEFT JOIN FETCH v.unit u " +
           "WHERE id.importRecord.importId = :importId " +
           "ORDER BY id.createdAt")
    List<ImportDetail> findByImportIdWithVariantDetails(@Param("importId") Integer importId);

    /**
     * Xóa tất cả chi tiết theo phiếu nhập
     * 
     * @param importId ID phiếu nhập
     */
    @Query("DELETE FROM ImportDetail id WHERE id.importRecord.importId = :importId")
    void deleteByImportId(@Param("importId") Integer importId);

    /**
     * Lấy lịch sử nhập hàng của một sản phẩm (tất cả biến thể)
     * 
     * @param productId ID sản phẩm
     * @return List<ImportDetail>
     */
    @Query("SELECT id FROM ImportDetail id " +
           "WHERE id.variant.product.id = :productId " +
           "ORDER BY id.createdAt DESC")
    List<ImportDetail> findByProductId(@Param("productId") Long productId);
}
