package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.PromotionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository interface cho PromotionDetail entity
 */
@Repository
public interface PromotionDetailRepository extends JpaRepository<PromotionDetail, Long> {

    /**
     * Tìm promotion detail theo promotion line ID
     */
    List<PromotionDetail> findByPromotionLineLineId(Long lineId);

    /**
     * Tìm promotion detail theo product unit ID (điều kiện)
     */
    List<PromotionDetail> findByConditionProductUnitId(Long productUnitId);

    /**
     * Tìm promotion detail theo category ID (điều kiện)
     */
    List<PromotionDetail> findByConditionCategoryCategoryId(Long categoryId);

    /**
     * Tìm promotion detail theo gift product unit ID
     */
    List<PromotionDetail> findByGiftProductUnitId(Long productUnitId);

    /**
     * Tìm promotion detail có giá trị đơn hàng tối thiểu
     */
    @Query("SELECT pd FROM PromotionDetail pd WHERE pd.minOrderValue <= :orderValue")
    List<PromotionDetail> findByMinOrderValueLessThanEqual(@Param("orderValue") BigDecimal orderValue);

    /**
     * Tìm promotion detail theo promotion line và có điều kiện product unit
     */
    @Query("SELECT pd FROM PromotionDetail pd WHERE pd.promotionLine.lineId = :lineId " +
           "AND pd.conditionProductUnit.id = :productUnitId")
    List<PromotionDetail> findByPromotionLineAndConditionProductUnit(
            @Param("lineId") Long lineId,
            @Param("productUnitId") Long productUnitId);

    /**
     * Tìm promotion detail theo promotion line và có điều kiện category
     */
    @Query("SELECT pd FROM PromotionDetail pd WHERE pd.promotionLine.lineId = :lineId " +
           "AND pd.conditionCategory.categoryId = :categoryId")
    List<PromotionDetail> findByPromotionLineAndConditionCategory(
            @Param("lineId") Long lineId,
            @Param("categoryId") Long categoryId);

    /**
     * Tìm promotion detail có loại BUY_X_GET_Y
     */
    @Query("SELECT pd FROM PromotionDetail pd WHERE pd.conditionBuyQuantity IS NOT NULL " +
           "AND pd.giftQuantity IS NOT NULL")
    List<PromotionDetail> findBuyXGetYPromotionDetails();

    /**
     * Tìm promotion detail có giá trị giảm giá tối đa
     */
    @Query("SELECT pd FROM PromotionDetail pd WHERE pd.maxDiscountValue IS NOT NULL")
    List<PromotionDetail> findPromotionDetailsWithMaxDiscount();

    /**
     * Tìm promotion detail áp dụng cho product unit cụ thể
     */
    @Query("SELECT pd FROM PromotionDetail pd " +
           "JOIN pd.promotionLine pl " +
           "WHERE pl.status = 'ACTIVE' " +
           "AND pl.startDate <= CURRENT_TIMESTAMP " +
           "AND pl.endDate >= CURRENT_TIMESTAMP " +
           "AND (pd.conditionProductUnit.id = :productUnitId " +
           "OR pd.conditionCategory.categoryId IN " +
           "(SELECT p.category.categoryId FROM ProductUnit pu " +
           "JOIN pu.product p WHERE pu.id = :productUnitId))")
    List<PromotionDetail> findApplicablePromotionDetailsForProductUnit(@Param("productUnitId") Long productUnitId);

    /**
     * Tìm promotion detail áp dụng cho category cụ thể
     */
    @Query("SELECT pd FROM PromotionDetail pd " +
           "JOIN pd.promotionLine pl " +
           "WHERE pl.status = 'ACTIVE' " +
           "AND pl.startDate <= CURRENT_TIMESTAMP " +
           "AND pl.endDate >= CURRENT_TIMESTAMP " +
           "AND pd.conditionCategory.categoryId = :categoryId")
    List<PromotionDetail> findApplicablePromotionDetailsForCategory(@Param("categoryId") Long categoryId);

    /**
     * Đếm số lượng promotion detail theo promotion line
     */
    long countByPromotionLineLineId(Long lineId);

    /**
     * Tìm promotion detail có giá trị khuyến mãi trong khoảng
     */
    @Query("SELECT pd FROM PromotionDetail pd WHERE pd.value BETWEEN :minValue AND :maxValue")
    List<PromotionDetail> findByValueBetween(
            @Param("minValue") BigDecimal minValue,
            @Param("maxValue") BigDecimal maxValue);

    /**
     * Xóa tất cả promotion detail theo promotion line ID
     */
    void deleteByPromotionLineLineId(Long lineId);

    /**
     * Tìm promotion detail có gift product unit
     */
    @Query("SELECT pd FROM PromotionDetail pd WHERE pd.giftProductUnit IS NOT NULL")
    List<PromotionDetail> findPromotionDetailsWithGiftProduct();
}
