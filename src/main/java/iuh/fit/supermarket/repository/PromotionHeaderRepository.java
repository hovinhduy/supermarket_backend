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
 */
@Repository
public interface PromotionHeaderRepository extends JpaRepository<PromotionHeader, Long> {

    /**
     * Tìm promotion theo tên
     */
    Optional<PromotionHeader> findByName(String name);

    /**
     * Tìm promotion theo trạng thái
     */
    List<PromotionHeader> findByStatus(PromotionStatus status);

    /**
     * Tìm promotion theo trạng thái với phân trang
     */
    Page<PromotionHeader> findByStatus(PromotionStatus status, Pageable pageable);

    /**
     * Tìm các promotion đang hoạt động trong khoảng thời gian
     */
    @Query("SELECT p FROM PromotionHeader p WHERE p.status = :status " +
           "AND p.startDate <= :endDate AND p.endDate >= :startDate")
    List<PromotionHeader> findActivePromotionsInDateRange(
            @Param("status") PromotionStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Tìm các promotion có thể áp dụng tại thời điểm hiện tại
     */
    @Query("SELECT p FROM PromotionHeader p WHERE p.status = 'ACTIVE' " +
           "AND p.startDate <= :currentTime AND p.endDate >= :currentTime")
    List<PromotionHeader> findCurrentActivePromotions(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Tìm promotion theo tên có chứa từ khóa (không phân biệt hoa thường)
     */
    @Query("SELECT p FROM PromotionHeader p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<PromotionHeader> findByNameContainingIgnoreCase(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Tìm promotion sắp hết hạn trong số ngày nhất định
     */
    @Query("SELECT p FROM PromotionHeader p WHERE p.status = 'ACTIVE' " +
           "AND p.endDate BETWEEN :now AND :futureDate")
    List<PromotionHeader> findPromotionsExpiringWithin(
            @Param("now") LocalDateTime now,
            @Param("futureDate") LocalDateTime futureDate);

    /**
     * Đếm số lượng promotion theo trạng thái
     */
    long countByStatus(PromotionStatus status);

    /**
     * Tìm promotion theo khoảng thời gian tạo
     */
    @Query("SELECT p FROM PromotionHeader p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    List<PromotionHeader> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Kiểm tra xem có promotion nào đang hoạt động trong khoảng thời gian không
     */
    @Query("SELECT COUNT(p) > 0 FROM PromotionHeader p WHERE p.status = 'ACTIVE' " +
           "AND p.startDate <= :endDate AND p.endDate >= :startDate")
    boolean existsActivePromotionInDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Tìm promotion có overlap về thời gian (trừ promotion hiện tại)
     */
    @Query("SELECT p FROM PromotionHeader p WHERE p.promotionId != :excludeId " +
           "AND p.startDate <= :endDate AND p.endDate >= :startDate")
    List<PromotionHeader> findOverlappingPromotions(
            @Param("excludeId") Long excludeId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
