package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.PromotionLine;
import iuh.fit.supermarket.enums.PromotionStatus;
import iuh.fit.supermarket.enums.PromotionType;
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
 * Repository interface cho PromotionLine entity
 */
@Repository
public interface PromotionLineRepository extends JpaRepository<PromotionLine, Long> {

    /**
     * Tìm promotion line theo mã
     */
    Optional<PromotionLine> findByLineCode(String lineCode);

    /**
     * Kiểm tra mã promotion line đã tồn tại
     */
    boolean existsByLineCode(String lineCode);

    /**
     * Tìm promotion line theo promotion header ID
     */
    List<PromotionLine> findByPromotionPromotionId(Long promotionId);

    /**
     * Tìm promotion line theo trạng thái
     */
    List<PromotionLine> findByStatus(PromotionStatus status);

    /**
     * Tìm promotion line theo loại khuyến mãi
     */
    List<PromotionLine> findByPromotionType(PromotionType promotionType);

    /**
     * Tìm các promotion line đang hoạt động
     */
    @Query("SELECT pl FROM PromotionLine pl WHERE pl.status = 'ACTIVE' " +
           "AND pl.startDate <= :currentTime AND pl.endDate >= :currentTime")
    List<PromotionLine> findCurrentActivePromotionLines(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Tìm promotion line theo promotion ID và trạng thái
     */
    List<PromotionLine> findByPromotionPromotionIdAndStatus(Long promotionId, PromotionStatus status);

    /**
     * Tìm promotion line có thể kết hợp
     */
    @Query("SELECT pl FROM PromotionLine pl WHERE pl.status = 'ACTIVE' " +
           "AND pl.isCombinable = true " +
           "AND pl.startDate <= :currentTime AND pl.endDate >= :currentTime")
    List<PromotionLine> findCombinableActivePromotionLines(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Tìm promotion line theo độ ưu tiên (sắp xếp giảm dần)
     */
    @Query("SELECT pl FROM PromotionLine pl WHERE pl.status = 'ACTIVE' " +
           "AND pl.startDate <= :currentTime AND pl.endDate >= :currentTime " +
           "ORDER BY pl.priority DESC")
    List<PromotionLine> findActivePromotionLinesByPriority(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Tìm promotion line có overlap về thời gian với promotion line khác
     */
    @Query("SELECT pl FROM PromotionLine pl WHERE pl.lineId != :excludeLineId " +
           "AND pl.startDate <= :endDate AND pl.endDate >= :startDate")
    List<PromotionLine> findOverlappingPromotionLines(
            @Param("excludeLineId") Long excludeLineId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Đếm số lượng promotion line theo trạng thái
     */
    long countByStatus(PromotionStatus status);

    /**
     * Tìm promotion line theo khoảng thời gian
     */
    @Query("SELECT pl FROM PromotionLine pl WHERE pl.startDate <= :endDate AND pl.endDate >= :startDate")
    List<PromotionLine> findPromotionLinesInDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Tìm promotion line sắp hết hạn
     */
    @Query("SELECT pl FROM PromotionLine pl WHERE pl.status = 'ACTIVE' " +
           "AND pl.endDate BETWEEN :now AND :futureDate")
    List<PromotionLine> findPromotionLinesExpiringWithin(
            @Param("now") LocalDateTime now,
            @Param("futureDate") LocalDateTime futureDate);

    /**
     * Tìm promotion line theo promotion header và loại khuyến mãi
     */
    List<PromotionLine> findByPromotionPromotionIdAndPromotionType(Long promotionId, PromotionType promotionType);

    /**
     * Tìm promotion line có giới hạn số lượng
     */
    @Query("SELECT pl FROM PromotionLine pl WHERE pl.maxTotalQuantity IS NOT NULL " +
           "AND pl.status = 'ACTIVE' " +
           "AND pl.startDate <= :currentTime AND pl.endDate >= :currentTime")
    List<PromotionLine> findLimitedQuantityPromotionLines(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Tìm promotion line có giới hạn per customer
     */
    @Query("SELECT pl FROM PromotionLine pl WHERE pl.maxPerCustomer IS NOT NULL " +
           "AND pl.status = 'ACTIVE' " +
           "AND pl.startDate <= :currentTime AND pl.endDate >= :currentTime")
    List<PromotionLine> findCustomerLimitedPromotionLines(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Tìm promotion line với phân trang
     */
    Page<PromotionLine> findByPromotionPromotionId(Long promotionId, Pageable pageable);
}
