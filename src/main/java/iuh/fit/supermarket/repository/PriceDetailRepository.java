package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.PriceDetail;
import iuh.fit.supermarket.enums.PriceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho entity PriceDetail
 */
@Repository
public interface PriceDetailRepository extends JpaRepository<PriceDetail, Long> {

    /**
     * Tìm tất cả chi tiết giá theo ID bảng giá
     */
    List<PriceDetail> findByPricePriceId(Long priceId);

    /**
     * Tìm chi tiết giá theo ID bảng giá và ID biến thể
     */
    Optional<PriceDetail> findByPricePriceIdAndVariantVariantId(Long priceId, Long variantId);

    /**
     * Kiểm tra biến thể đã tồn tại trong bảng giá chưa
     */
    boolean existsByPricePriceIdAndVariantVariantId(Long priceId, Long variantId);

    /**
     * Tìm chi tiết giá theo mã biến thể
     */
    @Query("SELECT pd FROM PriceDetail pd WHERE pd.variant.variantCode = :variantCode")
    List<PriceDetail> findByVariantCode(@Param("variantCode") String variantCode);

    /**
     * Tìm giá hiện tại của biến thể sản phẩm (từ bảng giá CURRENT)
     */
    @Query("SELECT pd FROM PriceDetail pd WHERE pd.variant.variantId = :variantId AND pd.price.status = :status")
    Optional<PriceDetail> findCurrentPriceByVariantId(@Param("variantId") Long variantId, @Param("status") PriceType status);

    /**
     * Tìm tất cả giá của biến thể sản phẩm theo trạng thái bảng giá
     */
    @Query("SELECT pd FROM PriceDetail pd WHERE pd.variant.variantId = :variantId AND pd.price.status = :status")
    List<PriceDetail> findByVariantIdAndPriceStatus(@Param("variantId") Long variantId, @Param("status") PriceType status);

    /**
     * Xóa tất cả chi tiết giá theo ID bảng giá
     */
    @Modifying
    @Query("DELETE FROM PriceDetail pd WHERE pd.price.priceId = :priceId")
    void deleteByPriceId(@Param("priceId") Long priceId);

    /**
     * Xóa chi tiết giá theo danh sách ID
     */
    @Modifying
    @Query("DELETE FROM PriceDetail pd WHERE pd.priceDetailId IN :priceDetailIds")
    void deleteByIds(@Param("priceDetailIds") List<Long> priceDetailIds);

    /**
     * Đếm số lượng chi tiết giá theo ID bảng giá
     */
    long countByPricePriceId(Long priceId);

    /**
     * Tìm chi tiết giá có giá trong khoảng
     */
    @Query("SELECT pd FROM PriceDetail pd WHERE pd.salePrice BETWEEN :minPrice AND :maxPrice")
    List<PriceDetail> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Tìm chi tiết giá theo ID bảng giá với thông tin biến thể
     */
    @Query("SELECT pd FROM PriceDetail pd " +
           "JOIN FETCH pd.variant v " +
           "WHERE pd.price.priceId = :priceId")
    List<PriceDetail> findByPriceIdWithVariant(@Param("priceId") Long priceId);

    /**
     * Tìm chi tiết giá theo danh sách ID biến thể và trạng thái bảng giá
     */
    @Query("SELECT pd FROM PriceDetail pd WHERE pd.variant.variantId IN :variantIds AND pd.price.status = :status")
    List<PriceDetail> findByVariantIdsAndPriceStatus(@Param("variantIds") List<Long> variantIds, @Param("status") PriceType status);

    /**
     * Kiểm tra biến thể có tồn tại trong bảng giá CURRENT khác không
     */
    @Query("SELECT COUNT(pd) > 0 FROM PriceDetail pd WHERE " +
           "pd.variant.variantId = :variantId AND pd.price.status = :status AND pd.price.priceId != :excludePriceId")
    boolean existsVariantInOtherCurrentPrice(@Param("variantId") Long variantId, 
                                           @Param("status") PriceType status, 
                                           @Param("excludePriceId") Long excludePriceId);

    /**
     * Lấy thống kê giá theo bảng giá
     */
    @Query("SELECT MIN(pd.salePrice), MAX(pd.salePrice), AVG(pd.salePrice), COUNT(pd) " +
           "FROM PriceDetail pd WHERE pd.price.priceId = :priceId")
    Object[] getPriceStatistics(@Param("priceId") Long priceId);
}
