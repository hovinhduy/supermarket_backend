//package iuh.fit.supermarket.repository;
//
//import iuh.fit.supermarket.entity.WarehouseTransaction;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
///**
// * Repository interface cho quản lý giao dịch kho
// */
//@Repository
//public interface WarehouseTransactionRepository extends JpaRepository<WarehouseTransaction, Long> {
//
//        /**
//         * Lấy danh sách giao dịch theo biến thể sản phẩm
//         *
//         * @param variantId ID biến thể sản phẩm
//         * @return List<WarehouseTransaction>
//         */
//        @Query("SELECT wt FROM WarehouseTransaction wt WHERE wt.variant.variantId = :variantId ORDER BY wt.transactionDate DESC")
//        List<WarehouseTransaction> findByVariantId(@Param("variantId") Long variantId);
//
//        /**
//         * Lấy danh sách giao dịch theo biến thể sản phẩm với phân trang
//         *
//         * @param variantId ID biến thể sản phẩm
//         * @param pageable  thông tin phân trang
//         * @return Page<WarehouseTransaction>
//         */
//        @Query("SELECT wt FROM WarehouseTransaction wt WHERE wt.variant.variantId = :variantId ORDER BY wt.transactionDate DESC")
//        Page<WarehouseTransaction> findByVariantId(@Param("variantId") Long variantId, Pageable pageable);
//
//        /**
//         * Lấy danh sách giao dịch theo loại giao dịch
//         *
//         * @param transactionType loại giao dịch
//         * @return List<WarehouseTransaction>
//         */
//        @Query("SELECT wt FROM WarehouseTransaction wt WHERE wt.transactionType = :transactionType ORDER BY wt.transactionDate DESC")
//        List<WarehouseTransaction> findByTransactionType(
//                        @Param("transactionType") WarehouseTransaction.TransactionType transactionType);
//
//        /**
//         * Lấy danh sách giao dịch theo loại giao dịch với phân trang
//         *
//         * @param transactionType loại giao dịch
//         * @param pageable        thông tin phân trang
//         * @return Page<WarehouseTransaction>
//         */
//        @Query("SELECT wt FROM WarehouseTransaction wt WHERE wt.transactionType = :transactionType ORDER BY wt.transactionDate DESC")
//        Page<WarehouseTransaction> findByTransactionType(
//                        @Param("transactionType") WarehouseTransaction.TransactionType transactionType,
//                        Pageable pageable);
//
//        /**
//         * Lấy danh sách giao dịch theo mã tham chiếu
//         *
//         * @param referenceId mã tham chiếu
//         * @return List<WarehouseTransaction>
//         */
//        @Query("SELECT wt FROM WarehouseTransaction wt WHERE wt.referenceId = :referenceId ORDER BY wt.transactionDate DESC")
//        List<WarehouseTransaction> findByReferenceId(@Param("referenceId") String referenceId);
//
//        /**
//         * Lấy danh sách giao dịch trong khoảng thời gian
//         *
//         * @param startDate ngày bắt đầu
//         * @param endDate   ngày kết thúc
//         * @return List<WarehouseTransaction>
//         */
//        @Query("SELECT wt FROM WarehouseTransaction wt WHERE wt.transactionDate BETWEEN :startDate AND :endDate ORDER BY wt.transactionDate DESC")
//        List<WarehouseTransaction> findByDateRange(@Param("startDate") LocalDateTime startDate,
//                        @Param("endDate") LocalDateTime endDate);
//
//        /**
//         * Lấy danh sách giao dịch trong khoảng thời gian với phân trang
//         *
//         * @param startDate ngày bắt đầu
//         * @param endDate   ngày kết thúc
//         * @param pageable  thông tin phân trang
//         * @return Page<WarehouseTransaction>
//         */
//        @Query("SELECT wt FROM WarehouseTransaction wt WHERE wt.transactionDate BETWEEN :startDate AND :endDate ORDER BY wt.transactionDate DESC")
//        Page<WarehouseTransaction> findByDateRange(@Param("startDate") LocalDateTime startDate,
//                        @Param("endDate") LocalDateTime endDate, Pageable pageable);
//
//        /**
//         * Lấy danh sách giao dịch theo biến thể và loại giao dịch
//         *
//         * @param variantId       ID biến thể sản phẩm
//         * @param transactionType loại giao dịch
//         * @return List<WarehouseTransaction>
//         */
//        @Query("SELECT wt FROM WarehouseTransaction wt WHERE wt.variant.variantId = :variantId AND wt.transactionType = :transactionType ORDER BY wt.transactionDate DESC")
//        List<WarehouseTransaction> findByVariantIdAndTransactionType(@Param("variantId") Long variantId,
//                        @Param("transactionType") WarehouseTransaction.TransactionType transactionType);
//
//        /**
//         * Lấy danh sách giao dịch theo biến thể trong khoảng thời gian
//         *
//         * @param variantId ID biến thể sản phẩm
//         * @param startDate ngày bắt đầu
//         * @param endDate   ngày kết thúc
//         * @return List<WarehouseTransaction>
//         */
//        @Query("SELECT wt FROM WarehouseTransaction wt WHERE wt.variant.variantId = :variantId AND wt.transactionDate BETWEEN :startDate AND :endDate ORDER BY wt.transactionDate DESC")
//        List<WarehouseTransaction> findByVariantIdAndDateRange(@Param("variantId") Long variantId,
//                        @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
//
//        /**
//         * Tính tổng số lượng thay đổi theo biến thể và loại giao dịch
//         *
//         * @param variantId       ID biến thể sản phẩm
//         * @param transactionType loại giao dịch
//         * @return tổng số lượng thay đổi
//         */
//        @Query("SELECT COALESCE(SUM(wt.quantityChange), 0) FROM WarehouseTransaction wt WHERE wt.variant.variantId = :variantId AND wt.transactionType = :transactionType")
//        Integer sumQuantityChangeByVariantIdAndTransactionType(@Param("variantId") Long variantId,
//                        @Param("transactionType") WarehouseTransaction.TransactionType transactionType);
//
//        /**
//         * Lấy giao dịch gần nhất theo biến thể
//         *
//         * @param variantId ID biến thể sản phẩm
//         * @return WarehouseTransaction
//         */
//        @Query("SELECT wt FROM WarehouseTransaction wt WHERE wt.variant.variantId = :variantId ORDER BY wt.transactionDate DESC LIMIT 1")
//        WarehouseTransaction findLatestByVariantId(@Param("variantId") Long variantId);
//
//        /**
//         * Lấy danh sách giao dịch với thông tin biến thể (để tránh N+1 query)
//         *
//         * @param pageable thông tin phân trang
//         * @return Page<WarehouseTransaction>
//         */
//        @Query("SELECT wt FROM WarehouseTransaction wt " +
//                        "LEFT JOIN FETCH wt.variant v " +
//                        "LEFT JOIN FETCH v.product p " +
//                        "LEFT JOIN FETCH v.unit u " +
//                        "ORDER BY wt.transactionDate DESC")
//        Page<WarehouseTransaction> findAllWithVariantDetails(Pageable pageable);
//
//        /**
//         * Đếm số lượng giao dịch theo loại
//         *
//         * @param transactionType loại giao dịch
//         * @return số lượng giao dịch
//         */
//        @Query("SELECT COUNT(wt) FROM WarehouseTransaction wt WHERE wt.transactionType = :transactionType")
//        Long countByTransactionType(@Param("transactionType") WarehouseTransaction.TransactionType transactionType);
//
//        /**
//         * Lấy danh sách giao dịch theo sản phẩm (tất cả biến thể)
//         *
//         * @param productId ID sản phẩm
//         * @return List<WarehouseTransaction>
//         */
//        @Query("SELECT wt FROM WarehouseTransaction wt WHERE wt.variant.product.id = :productId ORDER BY wt.transactionDate DESC")
//        List<WarehouseTransaction> findByProductId(@Param("productId") Long productId);
//}
