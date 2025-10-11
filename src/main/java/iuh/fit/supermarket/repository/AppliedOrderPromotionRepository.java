package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.AppliedOrderPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho quản lý khuyến mãi order level đã áp dụng
 */
@Repository
public interface AppliedOrderPromotionRepository extends JpaRepository<AppliedOrderPromotion, Long> {
    
    /**
     * Lấy danh sách khuyến mãi order đã áp dụng theo invoice
     */
    List<AppliedOrderPromotion> findByInvoice_InvoiceId(Integer invoiceId);
}
