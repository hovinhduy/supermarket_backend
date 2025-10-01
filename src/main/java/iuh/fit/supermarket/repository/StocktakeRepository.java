package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.Stocktake;
import iuh.fit.supermarket.enums.StocktakeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho quản lý phiếu kiểm kê kho
 */
@Repository
public interface StocktakeRepository extends JpaRepository<Stocktake, Integer> {

    /**
     * Tìm phiếu kiểm kê theo mã phiếu
     *
     * @param stocktakeCode mã phiếu kiểm kê
     * @return Optional<Stocktake>
     */
    Optional<Stocktake> findByStocktakeCode(String stocktakeCode);

    /**
     * Kiểm tra sự tồn tại của mã phiếu kiểm kê
     *
     * @param stocktakeCode mã phiếu kiểm kê
     * @return true nếu mã đã tồn tại
     */
    boolean existsByStocktakeCode(String stocktakeCode);

    /**
     * Lấy danh sách phiếu kiểm kê theo trạng thái
     *
     * @param status trạng thái kiểm kê
     * @return List<Stocktake>
     */
    List<Stocktake> findByStatus(StocktakeStatus status);

    /**
     * Lấy danh sách phiếu kiểm kê theo trạng thái với phân trang
     *
     * @param status   trạng thái kiểm kê
     * @param pageable thông tin phân trang
     * @return Page<Stocktake>
     */
    Page<Stocktake> findByStatus(StocktakeStatus status, Pageable pageable);

    /**
     * Lấy danh sách phiếu kiểm kê theo nhân viên tạo
     *
     * @param employeeId ID nhân viên tạo
     * @return List<Stocktake>
     */
    @Query("SELECT s FROM Stocktake s WHERE s.createdBy.employeeId = :employeeId ORDER BY s.createdAt DESC")
    List<Stocktake> findByCreatedBy(@Param("employeeId") Integer employeeId);

    /**
     * Lấy danh sách phiếu kiểm kê theo nhân viên hoàn thành
     *
     * @param employeeId ID nhân viên hoàn thành
     * @return List<Stocktake>
     */
    @Query("SELECT s FROM Stocktake s WHERE s.completedBy.employeeId = :employeeId ORDER BY s.completedAt DESC")
    List<Stocktake> findByCompletedBy(@Param("employeeId") Integer employeeId);

    /**
     * Lấy danh sách phiếu kiểm kê trong khoảng thời gian
     *
     * @param startDate ngày bắt đầu
     * @param endDate   ngày kết thúc
     * @return List<Stocktake>
     */
    @Query("SELECT s FROM Stocktake s WHERE s.createdAt BETWEEN :startDate AND :endDate ORDER BY s.createdAt DESC")
    List<Stocktake> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Lấy danh sách phiếu kiểm kê trong khoảng thời gian với phân trang
     *
     * @param startDate ngày bắt đầu
     * @param endDate   ngày kết thúc
     * @param pageable  thông tin phân trang
     * @return Page<Stocktake>
     */
    @Query("SELECT s FROM Stocktake s WHERE s.createdAt BETWEEN :startDate AND :endDate ORDER BY s.createdAt DESC")
    Page<Stocktake> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Tìm kiếm phiếu kiểm kê theo từ khóa (mã phiếu, ghi chú)
     *
     * @param keyword từ khóa tìm kiếm
     * @return List<Stocktake>
     */
    @Query("SELECT s FROM Stocktake s WHERE " +
            "LOWER(s.stocktakeCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.notes) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Stocktake> findByKeyword(@Param("keyword") String keyword);

    /**
     * Tìm kiếm phiếu kiểm kê theo từ khóa với phân trang
     *
     * @param keyword  từ khóa tìm kiếm
     * @param pageable thông tin phân trang
     * @return Page<Stocktake>
     */
    @Query("SELECT s FROM Stocktake s WHERE " +
            "LOWER(s.stocktakeCode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.notes) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Stocktake> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Lấy danh sách phiếu kiểm kê với thông tin nhân viên (để tránh N+1 query)
     *
     * @param pageable thông tin phân trang
     * @return Page<Stocktake>
     */
    @Query("SELECT s FROM Stocktake s " +
            "LEFT JOIN FETCH s.createdBy cb " +
            "LEFT JOIN FETCH s.completedBy cpb " +
            "ORDER BY s.createdAt DESC")
    Page<Stocktake> findAllWithEmployeeDetails(Pageable pageable);

    /**
     * Đếm số lượng phiếu kiểm kê theo trạng thái
     *
     * @param status trạng thái kiểm kê
     * @return số lượng phiếu kiểm kê
     */
    Long countByStatus(StocktakeStatus status);

    /**
     * Lấy phiếu kiểm kê gần nhất
     *
     * @return Optional<Stocktake>
     */
    @Query("SELECT s FROM Stocktake s ORDER BY s.createdAt DESC LIMIT 1")
    Optional<Stocktake> findLatest();

    /**
     * Lấy danh sách phiếu kiểm kê đã hoàn thành trong khoảng thời gian
     *
     * @param startDate ngày bắt đầu
     * @param endDate   ngày kết thúc
     * @return List<Stocktake>
     */
    @Query("SELECT s FROM Stocktake s WHERE s.status = 'COMPLETED' AND s.completedAt BETWEEN :startDate AND :endDate ORDER BY s.completedAt DESC")
    List<Stocktake> findCompletedBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Kiểm tra xem có phiếu kiểm kê nào đang PENDING không
     *
     * @return true nếu có phiếu kiểm kê đang PENDING
     */
    @Query("SELECT COUNT(s) > 0 FROM Stocktake s WHERE s.status = 'PENDING'")
    boolean existsPendingStocktake();

    /**
     * Lấy danh sách phiếu kiểm kê với chi tiết (để tránh N+1 query)
     *
     * @param stocktakeId ID phiếu kiểm kê
     * @return Optional<Stocktake>
     */
    @Query("SELECT s FROM Stocktake s " +
            "LEFT JOIN FETCH s.stocktakeDetails sd " +
            "LEFT JOIN FETCH sd.productUnit pu " +
            "LEFT JOIN FETCH pu.product p " +
            "LEFT JOIN FETCH pu.unit u " +
            "WHERE s.stocktakeId = :stocktakeId")
    Optional<Stocktake> findByIdWithDetails(@Param("stocktakeId") Integer stocktakeId);
}
