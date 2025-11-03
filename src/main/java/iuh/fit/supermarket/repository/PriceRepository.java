package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.Price;
import iuh.fit.supermarket.enums.PriceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho entity Price
 */
@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {

    /**
     * Tìm bảng giá theo mã
     */
    Optional<Price> findByPriceCode(String priceCode);

    /**
     * Kiểm tra mã bảng giá đã tồn tại chưa
     */
    boolean existsByPriceCode(String priceCode);

    /**
     * Kiểm tra mã bảng giá đã tồn tại chưa (trừ ID hiện tại)
     */
    boolean existsByPriceCodeAndPriceIdNot(String priceCode, Long priceId);

    /**
     * Tìm tất cả bảng giá theo trạng thái
     */
    List<Price> findByStatus(PriceType status);

    /**
     * Tìm bảng giá theo trạng thái với phân trang
     */
    Page<Price> findByStatus(PriceType status, Pageable pageable);

    /**
     * Tìm bảng giá cần chuyển từ UPCOMING sang CURRENT
     */
    @Query("SELECT p FROM Price p WHERE p.status = :status AND p.startDate <= :currentDate")
    List<Price> findPricesToActivate(@Param("status") PriceType status,
            @Param("currentDate") LocalDate currentDate);

    /**
     * Tìm bảng giá cần chuyển từ CURRENT sang EXPIRED
     */
    @Query("SELECT p FROM Price p WHERE p.status = :status AND p.endDate IS NOT NULL AND p.endDate <= :currentDate")
    List<Price> findPricesToExpire(@Param("status") PriceType status, @Param("currentDate") LocalDate currentDate);

    /**
     * Tìm kiếm bảng giá với điều kiện phức tạp
     */
    @Query("SELECT p FROM Price p WHERE " +
            "(:searchTerm IS NULL OR " +
            " LOWER(p.priceName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            " LOWER(p.priceCode) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:startDateFrom IS NULL OR p.startDate >= :startDateFrom) AND " +
            "(:startDateTo IS NULL OR p.startDate <= :startDateTo) AND " +
            "(:endDateFrom IS NULL OR p.endDate >= :endDateFrom) AND " +
            "(:endDateTo IS NULL OR p.endDate <= :endDateTo) AND " +
            "(:createdBy IS NULL OR p.createdBy.employeeId = :createdBy) AND " +
            "(:createdFrom IS NULL OR p.createdAt >= :createdFrom) AND " +
            "(:createdTo IS NULL OR p.createdAt <= :createdTo)")
    Page<Price> findPricesAdvanced(
            @Param("searchTerm") String searchTerm,
            @Param("status") PriceType status,
            @Param("startDateFrom") LocalDate startDateFrom,
            @Param("startDateTo") LocalDate startDateTo,
            @Param("endDateFrom") LocalDate endDateFrom,
            @Param("endDateTo") LocalDate endDateTo,
            @Param("createdBy") Integer createdBy,
            @Param("createdFrom") LocalDateTime createdFrom,
            @Param("createdTo") LocalDateTime createdTo,
            Pageable pageable);

    /**
     * Cập nhật trạng thái bảng giá theo danh sách ID
     */
    @Modifying
    @Query("UPDATE Price p SET p.status = :status WHERE p.priceId IN :priceIds")
    int updateStatusByIds(@Param("priceIds") List<Long> priceIds, @Param("status") PriceType status);

    /**
     * Tìm bảng giá CURRENT có chứa đơn vị sản phẩm cụ thể
     */
    @Query("SELECT p FROM Price p JOIN p.priceDetails pd WHERE p.status = :status AND pd.productUnit.id = :productUnitId")
    List<Price> findCurrentPricesByProductUnitId(@Param("status") PriceType status,
            @Param("productUnitId") Long productUnitId);

    /**
     * Kiểm tra đơn vị sản phẩm có tồn tại trong bảng giá CURRENT khác không
     */
    @Query("SELECT COUNT(p) > 0 FROM Price p JOIN p.priceDetails pd WHERE " +
            "p.status = :status AND pd.productUnit.id = :productUnitId AND p.priceId != :excludePriceId")
    boolean existsProductUnitInOtherCurrentPrice(@Param("status") PriceType status,
            @Param("productUnitId") Long productUnitId,
            @Param("excludePriceId") Long excludePriceId);

    /**
     * Đếm số lượng bảng giá theo trạng thái
     */
    long countByStatus(PriceType status);

    /**
     * Tìm bảng giá được tạo trong khoảng thời gian
     */
    List<Price> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Tìm bảng giá có ngày bắt đầu trong khoảng thời gian
     */
    List<Price> findByStartDateBetween(LocalDate startDate, LocalDate endDate);
}
