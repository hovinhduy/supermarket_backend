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
         * Tìm chi tiết giá theo ID bảng giá và ID đơn vị sản phẩm
         */
        Optional<PriceDetail> findByPricePriceIdAndProductUnitId(Long priceId, Long productUnitId);

        /**
         * Kiểm tra đơn vị sản phẩm đã tồn tại trong bảng giá chưa
         */
        boolean existsByPricePriceIdAndProductUnitId(Long priceId, Long productUnitId);

        /**
         * Tìm giá hiện tại của đơn vị sản phẩm (từ bảng giá CURRENT) với thông tin unit
         */
        @Query("SELECT pd FROM PriceDetail pd " +
                        "JOIN FETCH pd.productUnit pu " +
                        "JOIN FETCH pu.unit u " +
                        "WHERE pd.productUnit.id = :productUnitId AND pd.price.status = :status")
        Optional<PriceDetail> findCurrentPriceByProductUnitId(@Param("productUnitId") Long productUnitId,
                        @Param("status") PriceType status);

        /**
         * Tìm tất cả giá của đơn vị sản phẩm theo trạng thái bảng giá
         */
        @Query("SELECT pd FROM PriceDetail pd WHERE pd.productUnit.id = :productUnitId AND pd.price.status = :status")
        List<PriceDetail> findByProductUnitIdAndPriceStatus(@Param("productUnitId") Long productUnitId,
                        @Param("status") PriceType status);

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
        List<PriceDetail> findByPriceRange(@Param("minPrice") BigDecimal minPrice,
                        @Param("maxPrice") BigDecimal maxPrice);

        /**
         * Tìm chi tiết giá theo ID bảng giá với thông tin đơn vị sản phẩm và unit
         */
        @Query("SELECT pd FROM PriceDetail pd " +
                        "JOIN FETCH pd.productUnit pu " +
                        "JOIN FETCH pu.unit u " +
                        "WHERE pd.price.priceId = :priceId")
        List<PriceDetail> findByPriceIdWithProductUnit(@Param("priceId") Long priceId);

        /**
         * Tìm chi tiết giá theo danh sách ID đơn vị sản phẩm và trạng thái bảng giá
         */
        @Query("SELECT pd FROM PriceDetail pd WHERE pd.productUnit.id IN :productUnitIds AND pd.price.status = :status")
        List<PriceDetail> findByProductUnitIdsAndPriceStatus(@Param("productUnitIds") List<Long> productUnitIds,
                        @Param("status") PriceType status);

        /**
         * Kiểm tra đơn vị sản phẩm có tồn tại trong bảng giá CURRENT khác không
         */
        @Query("SELECT COUNT(pd) > 0 FROM PriceDetail pd WHERE " +
                        "pd.productUnit.id = :productUnitId AND pd.price.status = :status AND pd.price.priceId != :excludePriceId")
        boolean existsProductUnitInOtherCurrentPrice(@Param("productUnitId") Long productUnitId,
                        @Param("status") PriceType status,
                        @Param("excludePriceId") Long excludePriceId);

        /**
         * Lấy thống kê giá theo bảng giá
         */
        @Query("SELECT MIN(pd.salePrice), MAX(pd.salePrice), AVG(pd.salePrice), COUNT(pd) " +
                        "FROM PriceDetail pd WHERE pd.price.priceId = :priceId")
        Object[] getPriceStatistics(@Param("priceId") Long priceId);
}
