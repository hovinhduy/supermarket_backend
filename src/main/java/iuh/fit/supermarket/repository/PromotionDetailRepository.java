package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.PromotionDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface cho PromotionDetail entity
 * Cung cấp các phương thức truy vấn dữ liệu cho chi tiết khuyến mãi
 */
@Repository
public interface PromotionDetailRepository extends JpaRepository<PromotionDetail, Long> {

    /**
     * Tìm chi tiết khuyến mãi theo ID của promotion line
     * 
     * @param promotionLineId ID của PromotionLine
     * @return Optional chứa PromotionDetail nếu tìm thấy
     */
    Optional<PromotionDetail> findByPromotionLine_PromotionLineId(Long promotionLineId);

    /**
     * Xóa chi tiết khuyến mãi theo ID của promotion line
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
}
