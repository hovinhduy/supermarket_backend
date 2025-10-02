package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.PromotionHeader;
import iuh.fit.supermarket.enums.PromotionStatus;
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
 * Repository interface cho PromotionHeader entity
 * Cung cấp các phương thức truy vấn dữ liệu chương trình khuyến mãi
 */
@Repository
public interface PromotionHeaderRepository extends JpaRepository<PromotionHeader, Long> {

    /**
     * Tìm chương trình khuyến mãi theo tên (không phân biệt hoa thường)
     * 
     * @param promotionName tên chương trình khuyến mãi
     * @return Optional chứa PromotionHeader nếu tìm thấy
     */
    Optional<PromotionHeader> findByPromotionNameIgnoreCase(String promotionName);

    /**
     * Kiểm tra xem tên chương trình đã tồn tại chưa (trừ chương trình hiện tại)
     * 
     * @param promotionName tên chương trình
     * @param promotionId ID chương trình hiện tại (để loại trừ)
     * @return true nếu tên đã tồn tại
     */
    boolean existsByPromotionNameIgnoreCaseAndPromotionIdNot(String promotionName, Long promotionId);

    /**
     * Tìm tất cả chương trình theo trạng thái
     * 
     * @param status trạng thái cần tìm
     * @param pageable thông tin phân trang
     * @return Page chứa danh sách PromotionHeader
     */
    Page<PromotionHeader> findByStatus(PromotionStatus status, Pageable pageable);

    /**
     * Tìm chương trình theo khoảng thời gian bắt đầu
     * 
     * @param startDate ngày bắt đầu từ
     * @param endDate ngày bắt đầu đến
     * @param pageable thông tin phân trang
     * @return Page chứa danh sách PromotionHeader
     */
    Page<PromotionHeader> findByStartDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Tìm chương trình đang hoạt động tại thời điểm hiện tại
     * 
     * @param currentTime thời điểm hiện tại
     * @param pageable thông tin phân trang
     * @return Page chứa danh sách PromotionHeader đang hoạt động
     */
    @Query("SELECT ph FROM PromotionHeader ph WHERE ph.status = 'ACTIVE' " +
           "AND ph.startDate <= :currentTime AND ph.endDate >= :currentTime")
    Page<PromotionHeader> findActivePromotions(@Param("currentTime") LocalDateTime currentTime, Pageable pageable);

    /**
     * Tìm chương trình sắp diễn ra
     * 
     * @param currentTime thời điểm hiện tại
     * @param pageable thông tin phân trang
     * @return Page chứa danh sách PromotionHeader sắp diễn ra
     */
    @Query("SELECT ph FROM PromotionHeader ph WHERE ph.status IN ('UPCOMING', 'ACTIVE') " +
           "AND ph.startDate > :currentTime")
    Page<PromotionHeader> findUpcomingPromotions(@Param("currentTime") LocalDateTime currentTime, Pageable pageable);

    /**
     * Tìm chương trình đã hết hạn
     * 
     * @param currentTime thời điểm hiện tại
     * @param pageable thông tin phân trang
     * @return Page chứa danh sách PromotionHeader đã hết hạn
     */
    @Query("SELECT ph FROM PromotionHeader ph WHERE ph.endDate < :currentTime")
    Page<PromotionHeader> findExpiredPromotions(@Param("currentTime") LocalDateTime currentTime, Pageable pageable);

    /**
     * Tìm kiếm chương trình theo từ khóa (tên hoặc mô tả)
     * 
     * @param keyword từ khóa tìm kiếm
     * @param pageable thông tin phân trang
     * @return Page chứa danh sách PromotionHeader phù hợp
     */
    @Query("SELECT ph FROM PromotionHeader ph WHERE " +
           "LOWER(ph.promotionName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(ph.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<PromotionHeader> findByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Tìm kiếm chương trình với nhiều điều kiện
     * 
     * @param keyword từ khóa tìm kiếm
     * @param status trạng thái
     * @param startDateFrom ngày bắt đầu từ
     * @param startDateTo ngày bắt đầu đến
     * @param endDateFrom ngày kết thúc từ
     * @param endDateTo ngày kết thúc đến
     * @param pageable thông tin phân trang
     * @return Page chứa danh sách PromotionHeader phù hợp
     */
    @Query("SELECT ph FROM PromotionHeader ph WHERE " +
           "(:keyword IS NULL OR LOWER(ph.promotionName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(ph.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:status IS NULL OR ph.status = :status) AND " +
           "(:startDateFrom IS NULL OR ph.startDate >= :startDateFrom) AND " +
           "(:startDateTo IS NULL OR ph.startDate <= :startDateTo) AND " +
           "(:endDateFrom IS NULL OR ph.endDate >= :endDateFrom) AND " +
           "(:endDateTo IS NULL OR ph.endDate <= :endDateTo)")
    Page<PromotionHeader> findWithCriteria(
        @Param("keyword") String keyword,
        @Param("status") PromotionStatus status,
        @Param("startDateFrom") LocalDateTime startDateFrom,
        @Param("startDateTo") LocalDateTime startDateTo,
        @Param("endDateFrom") LocalDateTime endDateFrom,
        @Param("endDateTo") LocalDateTime endDateTo,
        Pageable pageable
    );

    /**
     * Đếm số lượng chương trình theo trạng thái
     * 
     * @param status trạng thái cần đếm
     * @return số lượng chương trình
     */
    long countByStatus(PromotionStatus status);

    /**
     * Tìm chương trình có thời gian trùng lặp
     * 
     * @param startDate ngày bắt đầu
     * @param endDate ngày kết thúc
     * @param excludeId ID chương trình cần loại trừ
     * @return danh sách chương trình trùng lặp thời gian
     */
    @Query("SELECT ph FROM PromotionHeader ph WHERE " +
           "(:excludeId IS NULL OR ph.promotionId != :excludeId) AND " +
           "ph.status IN ('ACTIVE', 'UPCOMING') AND " +
           "((ph.startDate <= :endDate AND ph.endDate >= :startDate))")
    List<PromotionHeader> findOverlappingPromotions(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("excludeId") Long excludeId
    );

    /**
     * Cập nhật trạng thái chương trình đã hết hạn
     * 
     * @param currentTime thời điểm hiện tại
     * @return số lượng bản ghi được cập nhật
     */
    @Query("UPDATE PromotionHeader ph SET ph.status = 'EXPIRED' " +
           "WHERE ph.endDate < :currentTime AND ph.status IN ('ACTIVE', 'UPCOMING')")
    int updateExpiredPromotions(@Param("currentTime") LocalDateTime currentTime);
}
