package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.PromotionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho PromotionDetail entity
 * Cung cấp các phương thức truy vấn dữ liệu cho chi tiết khuyến mãi
 */
@Repository
public interface PromotionDetailRepository extends JpaRepository<PromotionDetail, Long> {

    /**
     * Tìm tất cả chi tiết khuyến mãi theo ID của promotion line
     * 
     * @param promotionLineId ID của PromotionLine
     * @return List chứa các PromotionDetail
     */
    List<PromotionDetail> findByPromotionLine_PromotionLineId(Long promotionLineId);

    /**
     * Xóa tất cả chi tiết khuyến mãi theo ID của promotion line
     * 
     * @param promotionLineId ID của PromotionLine
     */
    void deleteByPromotionLine_PromotionLineId(Long promotionLineId);

    /**
     * Kiểm tra xem promotion line đã có chi tiết chưa
     * 
     * @param promotionLineId ID của PromotionLine
     * @return true nếu đã có chi tiết
     */
    boolean existsByPromotionLine_PromotionLineId(Long promotionLineId);
    
    /**
     * Đếm số lượng chi tiết của một promotion line
     * 
     * @param promotionLineId ID của PromotionLine
     * @return số lượng chi tiết
     */
    long countByPromotionLine_PromotionLineId(Long promotionLineId);
}
