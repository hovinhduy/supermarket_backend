package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.PromotionLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface cho PromotionLine entity
 * Cung cấp các phương thức truy vấn dữ liệu cho các line khuyến mãi
 */
@Repository
public interface PromotionLineRepository extends JpaRepository<PromotionLine, Long> {

    /**
     * Tìm tất cả các line thuộc về một chương trình khuyến mãi (header)
     *
     * @param promotionId ID của PromotionHeader
     * @return danh sách các PromotionLine
     */
    @Query("SELECT pl FROM PromotionLine pl WHERE pl.header.promotionId = :promotionId")
    List<PromotionLine> findByPromotionHeaderId(@Param("promotionId") Long promotionId);

    /**
     * Xóa tất cả các line thuộc về một chương trình khuyến mãi
     * 
     * @param promotionId ID của PromotionHeader
     */
    void deleteByHeader_PromotionId(Long promotionId);

    /**
     * Đếm số lượng line thuộc về một chương trình khuyến mãi
     * 
     * @param promotionId ID của PromotionHeader
     * @return số lượng line
     */
    long countByHeader_PromotionId(Long promotionId);
}
