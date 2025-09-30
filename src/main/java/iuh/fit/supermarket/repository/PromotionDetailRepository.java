package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.PromotionDetail;
import iuh.fit.supermarket.entity.PromotionHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository để thao tác với bảng promotion_detail
 */
@Repository
public interface PromotionDetailRepository extends JpaRepository<PromotionDetail, Long> {

    /**
     * Tìm tất cả chi tiết khuyến mãi theo ID chương trình khuyến mãi
     * 
     * @param promotionId ID chương trình khuyến mãi
     * @return danh sách PromotionDetail
     */
    @Query("SELECT pd FROM PromotionDetail pd WHERE pd.promotion.promotionId = :promotionId")
    List<PromotionDetail> findByPromotionId(@Param("promotionId") Long promotionId);

    /**
     * Xóa tất cả chi tiết khuyến mãi theo ID chương trình khuyến mãi
     * 
     * @param promotionId ID chương trình khuyến mãi
     */
    @Query("DELETE FROM PromotionDetail pd WHERE pd.promotion.promotionId = :promotionId")
    void deleteByPromotionId(@Param("promotionId") Long promotionId);

    /**
     * Tìm chi tiết khuyến mãi theo sản phẩm mua (BUY_X_GET_Y)
     * 
     * @param productId ID sản phẩm
     * @return danh sách PromotionDetail
     */
    @Query("SELECT pd FROM PromotionDetail pd WHERE pd.buyProduct.id = :productId")
    List<PromotionDetail> findByBuyProductId(@Param("productId") Long productId);

    /**
     * Tìm chi tiết khuyến mãi theo sản phẩm được tặng (BUY_X_GET_Y)
     * 
     * @param productId ID sản phẩm
     * @return danh sách PromotionDetail
     */
    @Query("SELECT pd FROM PromotionDetail pd WHERE pd.giftProduct.id = :productId")
    List<PromotionDetail> findByGiftProductId(@Param("productId") Long productId);

    /**
     * Tìm chi tiết khuyến mãi theo danh mục (BUY_X_GET_Y)
     * 
     * @param categoryId ID danh mục
     * @return danh sách PromotionDetail
     */
    @Query("SELECT pd FROM PromotionDetail pd WHERE pd.buyCategory.categoryId = :categoryId")
    List<PromotionDetail> findByBuyCategoryId(@Param("categoryId") Integer categoryId);

    /**
     * Tìm chi tiết khuyến mãi theo sản phẩm áp dụng (PRODUCT_DISCOUNT)
     * 
     * @param productId ID sản phẩm
     * @return danh sách PromotionDetail
     */
    @Query("SELECT pd FROM PromotionDetail pd WHERE pd.applyToProduct.id = :productId")
    List<PromotionDetail> findByApplyToProductId(@Param("productId") Long productId);

    /**
     * Tìm chi tiết khuyến mãi theo danh mục áp dụng (PRODUCT_DISCOUNT)
     * 
     * @param categoryId ID danh mục
     * @return danh sách PromotionDetail
     */
    @Query("SELECT pd FROM PromotionDetail pd WHERE pd.applyToCategory.categoryId = :categoryId")
    List<PromotionDetail> findByApplyToCategoryId(@Param("categoryId") Integer categoryId);

    /**
     * Đếm số lượng chi tiết khuyến mãi theo chương trình khuyến mãi
     * 
     * @param promotion chương trình khuyến mãi
     * @return số lượng chi tiết
     */
    long countByPromotion(PromotionHeader promotion);
}

