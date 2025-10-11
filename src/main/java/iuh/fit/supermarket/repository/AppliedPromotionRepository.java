package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.AppliedPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho quản lý khuyến mãi đã áp dụng
 */
@Repository
public interface AppliedPromotionRepository extends JpaRepository<AppliedPromotion, Long> {
    
    /**
     * Lấy danh sách khuyến mãi đã áp dụng theo chi tiết hóa đơn
     */
    List<AppliedPromotion> findByInvoiceDetail_InvoiceDetailId(Integer invoiceDetailId);
}
