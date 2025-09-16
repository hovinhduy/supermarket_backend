package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository cho quản lý giao dịch tồn kho
 */
@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

        /**
         * Lấy lịch sử giao dịch theo biến thể sản phẩm
         * Sử dụng @EntityGraph để eager load ProductVariant và Warehouse
         *
         * @param variantId ID biến thể sản phẩm
         * @return danh sách giao dịch theo thời gian giảm dần
         */
        @EntityGraph(attributePaths = { "variant", "warehouse" })
        List<InventoryTransaction> findByVariantVariantIdOrderByTransactionDateDesc(Long variantId);

        /**
         * Lấy lịch sử giao dịch theo biến thể và kho hàng
         * 
         * @param variantId   ID biến thể sản phẩm
         * @param warehouseId ID kho hàng
         * @return danh sách giao dịch theo thời gian giảm dần
         */
        List<InventoryTransaction> findByVariantVariantIdAndWarehouseWarehouseIdOrderByTransactionDateDesc(
                        Long variantId, Integer warehouseId);

        /**
         * Lấy lịch sử giao dịch theo kho hàng
         * 
         * @param warehouseId ID kho hàng
         * @return danh sách giao dịch theo thời gian giảm dần
         */
        List<InventoryTransaction> findByWarehouseWarehouseIdOrderByTransactionDateDesc(Integer warehouseId);

        /**
         * Lấy lịch sử giao dịch theo loại giao dịch
         * 
         * @param transactionType loại giao dịch
         * @return danh sách giao dịch theo thời gian giảm dần
         */
        List<InventoryTransaction> findByTransactionTypeOrderByTransactionDateDesc(
                        InventoryTransaction.TransactionType transactionType);

        /**
         * Lấy lịch sử giao dịch trong khoảng thời gian
         * 
         * @param startDate thời gian bắt đầu
         * @param endDate   thời gian kết thúc
         * @return danh sách giao dịch trong khoảng thời gian
         */
        List<InventoryTransaction> findByTransactionDateBetweenOrderByTransactionDateDesc(
                        LocalDateTime startDate, LocalDateTime endDate);

        /**
         * Lấy giao dịch gần nhất của một biến thể tại một kho
         * 
         * @param variantId   ID biến thể sản phẩm
         * @param warehouseId ID kho hàng
         * @return giao dịch gần nhất
         */
        @Query("SELECT it FROM InventoryTransaction it WHERE it.variant.variantId = :variantId " +
                        "AND it.warehouse.warehouseId = :warehouseId ORDER BY it.transactionDate DESC LIMIT 1")
        InventoryTransaction findLatestTransactionByVariantAndWarehouse(
                        @Param("variantId") Long variantId, @Param("warehouseId") Integer warehouseId);

        /**
         * Tính tổng số lượng nhập của một biến thể
         * 
         * @param variantId ID biến thể sản phẩm
         * @return tổng số lượng nhập
         */
        @Query("SELECT COALESCE(SUM(it.quantityChange), 0) FROM InventoryTransaction it " +
                        "WHERE it.variant.variantId = :variantId AND it.quantityChange > 0")
        Integer getTotalStockInByVariantId(@Param("variantId") Long variantId);

        /**
         * Tính tổng số lượng xuất của một biến thể
         *
         * @param variantId ID biến thể sản phẩm
         * @return tổng số lượng xuất (số âm)
         */
        @Query("SELECT COALESCE(SUM(it.quantityChange), 0) FROM InventoryTransaction it " +
                        "WHERE it.variant.variantId = :variantId AND it.quantityChange < 0")
        Integer getTotalStockOutByVariantId(@Param("variantId") Long variantId);

        /**
         * Lấy lịch sử giao dịch kho với tìm kiếm và phân trang
         * Tìm kiếm theo mã sản phẩm, mã biến thể hoặc tên sản phẩm
         *
         * @param searchTerm từ khóa tìm kiếm
         * @param pageable   thông tin phân trang và sắp xếp
         * @return danh sách giao dịch với phân trang
         */
        @EntityGraph(attributePaths = { "variant", "warehouse" })
        @Query("SELECT it FROM InventoryTransaction it " +
                        "WHERE (:searchTerm = '' OR " +
                        "       it.variant.variantCode LIKE %:searchTerm% OR " +
                        "       it.variant.variantName LIKE %:searchTerm% OR " +
                        "       it.variant.product.code LIKE %:searchTerm% OR " +
                        "       it.variant.product.name LIKE %:searchTerm%)")
        Page<InventoryTransaction> findInventoryHistoryWithSearch(@Param("searchTerm") String searchTerm,
                        Pageable pageable);
}
