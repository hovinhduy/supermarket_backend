package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.StocktakeDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho quản lý chi tiết phiếu kiểm kê kho
 */
@Repository
public interface StocktakeDetailRepository extends JpaRepository<StocktakeDetail, Integer> {

    /**
     * Lấy danh sách chi tiết theo phiếu kiểm kê
     *
     * @param stocktakeId ID phiếu kiểm kê
     * @return List<StocktakeDetail>
     */
    @Query("SELECT sd FROM StocktakeDetail sd WHERE sd.stocktake.stocktakeId = :stocktakeId ORDER BY sd.createdAt")
    List<StocktakeDetail> findByStocktakeId(@Param("stocktakeId") Integer stocktakeId);

    /**
     * Lấy danh sách chi tiết theo đơn vị sản phẩm
     *
     * @param productUnitId ID đơn vị sản phẩm
     * @return List<StocktakeDetail>
     */
    @Query("SELECT sd FROM StocktakeDetail sd WHERE sd.productUnit.id = :productUnitId ORDER BY sd.createdAt DESC")
    List<StocktakeDetail> findByProductUnitId(@Param("productUnitId") Long productUnitId);

    /**
     * Tìm chi tiết kiểm kê theo phiếu kiểm kê và đơn vị sản phẩm
     *
     * @param stocktakeId   ID phiếu kiểm kê
     * @param productUnitId ID đơn vị sản phẩm
     * @return Optional<StocktakeDetail>
     */
    @Query("SELECT sd FROM StocktakeDetail sd WHERE sd.stocktake.stocktakeId = :stocktakeId AND sd.productUnit.id = :productUnitId")
    Optional<StocktakeDetail> findByStocktakeIdAndProductUnitId(@Param("stocktakeId") Integer stocktakeId,
            @Param("productUnitId") Long productUnitId);

    /**
     * Kiểm tra sự tồn tại của chi tiết kiểm kê theo phiếu kiểm kê và đơn vị sản
     * phẩm
     *
     * @param stocktakeId   ID phiếu kiểm kê
     * @param productUnitId ID đơn vị sản phẩm
     * @return true nếu đã tồn tại
     */
    @Query("SELECT COUNT(sd) > 0 FROM StocktakeDetail sd WHERE sd.stocktake.stocktakeId = :stocktakeId AND sd.productUnit.id = :productUnitId")
    boolean existsByStocktakeIdAndProductUnitId(@Param("stocktakeId") Integer stocktakeId,
            @Param("productUnitId") Long productUnitId);

    /**
     * Lấy danh sách chi tiết có chênh lệch (khác 0)
     *
     * @param stocktakeId ID phiếu kiểm kê
     * @return List<StocktakeDetail>
     */
    @Query("SELECT sd FROM StocktakeDetail sd WHERE sd.stocktake.stocktakeId = :stocktakeId AND sd.quantityDifference != 0 ORDER BY ABS(sd.quantityDifference) DESC")
    List<StocktakeDetail> findDifferencesByStocktakeId(@Param("stocktakeId") Integer stocktakeId);

    /**
     * Lấy danh sách chi tiết có chênh lệch dương (thừa)
     *
     * @param stocktakeId ID phiếu kiểm kê
     * @return List<StocktakeDetail>
     */
    @Query("SELECT sd FROM StocktakeDetail sd WHERE sd.stocktake.stocktakeId = :stocktakeId AND sd.quantityDifference > 0 ORDER BY sd.quantityDifference DESC")
    List<StocktakeDetail> findPositiveDifferencesByStocktakeId(@Param("stocktakeId") Integer stocktakeId);

    /**
     * Lấy danh sách chi tiết có chênh lệch âm (thiếu)
     *
     * @param stocktakeId ID phiếu kiểm kê
     * @return List<StocktakeDetail>
     */
    @Query("SELECT sd FROM StocktakeDetail sd WHERE sd.stocktake.stocktakeId = :stocktakeId AND sd.quantityDifference < 0 ORDER BY sd.quantityDifference ASC")
    List<StocktakeDetail> findNegativeDifferencesByStocktakeId(@Param("stocktakeId") Integer stocktakeId);

    /**
     * Đếm số lượng chi tiết theo phiếu kiểm kê
     *
     * @param stocktakeId ID phiếu kiểm kê
     * @return số lượng chi tiết
     */
    @Query("SELECT COUNT(sd) FROM StocktakeDetail sd WHERE sd.stocktake.stocktakeId = :stocktakeId")
    Long countByStocktakeId(@Param("stocktakeId") Integer stocktakeId);

    /**
     * Đếm số lượng chi tiết có chênh lệch theo phiếu kiểm kê
     *
     * @param stocktakeId ID phiếu kiểm kê
     * @return số lượng chi tiết có chênh lệch
     */
    @Query("SELECT COUNT(sd) FROM StocktakeDetail sd WHERE sd.stocktake.stocktakeId = :stocktakeId AND sd.quantityDifference != 0")
    Long countDifferencesByStocktakeId(@Param("stocktakeId") Integer stocktakeId);

    /**
     * Tính tổng chênh lệch dương theo phiếu kiểm kê
     *
     * @param stocktakeId ID phiếu kiểm kê
     * @return tổng chênh lệch dương
     */
    @Query("SELECT COALESCE(SUM(sd.quantityDifference), 0) FROM StocktakeDetail sd WHERE sd.stocktake.stocktakeId = :stocktakeId AND sd.quantityDifference > 0")
    Integer sumPositiveDifferencesByStocktakeId(@Param("stocktakeId") Integer stocktakeId);

    /**
     * Tính tổng chênh lệch âm theo phiếu kiểm kê
     *
     * @param stocktakeId ID phiếu kiểm kê
     * @return tổng chênh lệch âm
     */
    @Query("SELECT COALESCE(SUM(sd.quantityDifference), 0) FROM StocktakeDetail sd WHERE sd.stocktake.stocktakeId = :stocktakeId AND sd.quantityDifference < 0")
    Integer sumNegativeDifferencesByStocktakeId(@Param("stocktakeId") Integer stocktakeId);

    /**
     * Lấy danh sách chi tiết với thông tin đơn vị sản phẩm (để tránh N+1 query)
     *
     * @param stocktakeId ID phiếu kiểm kê
     * @return List<StocktakeDetail>
     */
    @Query("SELECT sd FROM StocktakeDetail sd " +
            "LEFT JOIN FETCH sd.productUnit pu " +
            "LEFT JOIN FETCH pu.product p " +
            "LEFT JOIN FETCH pu.unit u " +
            "WHERE sd.stocktake.stocktakeId = :stocktakeId " +
            "ORDER BY sd.createdAt")
    List<StocktakeDetail> findByStocktakeIdWithProductUnitDetails(@Param("stocktakeId") Integer stocktakeId);

    /**
     * Xóa tất cả chi tiết theo phiếu kiểm kê
     *
     * @param stocktakeId ID phiếu kiểm kê
     */
    @Query("DELETE FROM StocktakeDetail sd WHERE sd.stocktake.stocktakeId = :stocktakeId")
    void deleteByStocktakeId(@Param("stocktakeId") Integer stocktakeId);

    /**
     * Lấy lịch sử kiểm kê của một đơn vị sản phẩm
     *
     * @param productUnitId ID đơn vị sản phẩm
     * @return List<StocktakeDetail>
     */
    @Query("SELECT sd FROM StocktakeDetail sd " +
            "WHERE sd.productUnit.id = :productUnitId " +
            "ORDER BY sd.createdAt DESC")
    List<StocktakeDetail> findHistoryByProductUnitId(@Param("productUnitId") Long productUnitId);

    /**
     * Lấy lịch sử kiểm kê của một sản phẩm (tất cả đơn vị)
     *
     * @param productId ID sản phẩm
     * @return List<StocktakeDetail>
     */
    @Query("SELECT sd FROM StocktakeDetail sd " +
            "WHERE sd.productUnit.product.id = :productId " +
            "ORDER BY sd.createdAt DESC")
    List<StocktakeDetail> findHistoryByProductId(@Param("productId") Long productId);

    /**
     * Tìm kiếm chi tiết kiểm kê theo từ khóa (tên sản phẩm, mã đơn vị sản phẩm, mã
     * vạch)
     *
     * @param stocktakeId ID phiếu kiểm kê
     * @param keyword     từ khóa tìm kiếm
     * @return List<StocktakeDetail>
     */
    @Query("SELECT sd FROM StocktakeDetail sd WHERE sd.stocktake.stocktakeId = :stocktakeId AND (" +
            "LOWER(sd.productUnit.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(sd.productUnit.barcode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(sd.productUnit.product.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<StocktakeDetail> findByStocktakeIdAndKeyword(@Param("stocktakeId") Integer stocktakeId,
            @Param("keyword") String keyword);
}
