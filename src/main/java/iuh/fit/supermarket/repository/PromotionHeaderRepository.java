package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.PromotionHeader;
import iuh.fit.supermarket.enums.PromotionStatus;
import iuh.fit.supermarket.enums.PromotionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository để thao tác với bảng promotion_header
 */
@Repository
public interface PromotionHeaderRepository extends JpaRepository<PromotionHeader, Long> {

    /**
     * Tìm kiếm khuyến mãi theo mã
     * 
     * @param promotionCode mã khuyến mãi
     * @return Optional chứa PromotionHeader nếu tìm thấy
     */
    Optional<PromotionHeader> findByPromotionCode(String promotionCode);

    /**
     * Kiểm tra xem mã khuyến mãi đã tồn tại chưa
     * 
     * @param promotionCode mã khuyến mãi
     * @return true nếu đã tồn tại, ngược lại false
     */
    boolean existsByPromotionCode(String promotionCode);

    /**
     * Tìm tất cả khuyến mãi theo trạng thái
     * 
     * @param status   trạng thái khuyến mãi
     * @param pageable thông tin phân trang
     * @return Page chứa danh sách PromotionHeader
     */
    Page<PromotionHeader> findByStatus(PromotionStatus status, Pageable pageable);

    /**
     * Tìm tất cả khuyến mãi theo loại
     * 
     * @param promotionType loại khuyến mãi
     * @param pageable      thông tin phân trang
     * @return Page chứa danh sách PromotionHeader
     */
    Page<PromotionHeader> findByPromotionType(PromotionType promotionType, Pageable pageable);

    /**
     * Tìm khuyến mãi theo ID với chi tiết khuyến mãi
     * 
     * @param promotionId ID khuyến mãi
     * @return Optional chứa PromotionHeader với chi tiết nếu tìm thấy
     */
    @EntityGraph(attributePaths = { "promotionDetails" })
    @Query("SELECT p FROM PromotionHeader p WHERE p.promotionId = :promotionId")
    Optional<PromotionHeader> findByIdWithDetails(@Param("promotionId") Long promotionId);

    /**
     * Tìm tất cả khuyến mãi đang hoạt động trong khoảng thời gian
     * 
     * @param currentDate thời điểm hiện tại
     * @param status      trạng thái ACTIVE
     * @return danh sách PromotionHeader
     */
    @EntityGraph(attributePaths = { "promotionDetails" })
    @Query("SELECT p FROM PromotionHeader p WHERE p.status = :status " +
            "AND p.startDate <= :currentDate AND p.endDate >= :currentDate")
    List<PromotionHeader> findActivePromotions(
            @Param("currentDate") LocalDateTime currentDate,
            @Param("status") PromotionStatus status);

    /**
     * Tìm kiếm khuyến mãi theo từ khóa (tên hoặc mã)
     * 
     * @param keyword  từ khóa tìm kiếm
     * @param pageable thông tin phân trang
     * @return Page chứa danh sách PromotionHeader
     */
    @Query("SELECT p FROM PromotionHeader p WHERE " +
            "LOWER(p.promotionName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.promotionCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<PromotionHeader> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Tìm kiếm khuyến mãi theo nhiều tiêu chí
     * 
     * @param keyword       từ khóa tìm kiếm
     * @param promotionType loại khuyến mãi
     * @param status        trạng thái
     * @param startDateFrom ngày bắt đầu từ
     * @param startDateTo   ngày bắt đầu đến
     * @param endDateFrom   ngày kết thúc từ
     * @param endDateTo     ngày kết thúc đến
     * @param pageable      thông tin phân trang
     * @return Page chứa danh sách PromotionHeader
     */
    @Query("SELECT p FROM PromotionHeader p WHERE " +
            "(:keyword IS NULL OR LOWER(p.promotionName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.promotionCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:promotionType IS NULL OR p.promotionType = :promotionType) AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:startDateFrom IS NULL OR p.startDate >= :startDateFrom) AND " +
            "(:startDateTo IS NULL OR p.startDate <= :startDateTo) AND " +
            "(:endDateFrom IS NULL OR p.endDate >= :endDateFrom) AND " +
            "(:endDateTo IS NULL OR p.endDate <= :endDateTo)")
    Page<PromotionHeader> searchPromotions(
            @Param("keyword") String keyword,
            @Param("promotionType") PromotionType promotionType,
            @Param("status") PromotionStatus status,
            @Param("startDateFrom") LocalDateTime startDateFrom,
            @Param("startDateTo") LocalDateTime startDateTo,
            @Param("endDateFrom") LocalDateTime endDateFrom,
            @Param("endDateTo") LocalDateTime endDateTo,
            Pageable pageable);

    /**
     * Tìm các khuyến mãi đang sắp hết hạn
     * 
     * @param currentDate    thời điểm hiện tại
     * @param expirationDate thời điểm sắp hết hạn
     * @param status         trạng thái ACTIVE
     * @return danh sách PromotionHeader
     */
    @Query("SELECT p FROM PromotionHeader p WHERE p.status = :status " +
            "AND p.endDate > :currentDate AND p.endDate <= :expirationDate")
    List<PromotionHeader> findExpiringPromotions(
            @Param("currentDate") LocalDateTime currentDate,
            @Param("expirationDate") LocalDateTime expirationDate,
            @Param("status") PromotionStatus status);

    /**
     * Đếm số lượng khuyến mãi theo trạng thái
     * 
     * @param status trạng thái khuyến mãi
     * @return số lượng khuyến mãi
     */
    long countByStatus(PromotionStatus status);

    /**
     * Tìm các khuyến mãi theo loại và trạng thái
     * 
     * @param promotionType loại khuyến mãi
     * @param status        trạng thái
     * @return danh sách PromotionHeader
     */
    @EntityGraph(attributePaths = { "promotionDetails" })
    @Query("SELECT p FROM PromotionHeader p WHERE p.promotionType = :promotionType " +
            "AND p.status = :status")
    List<PromotionHeader> findByTypeAndStatus(
            @Param("promotionType") PromotionType promotionType,
            @Param("status") PromotionStatus status);
}

