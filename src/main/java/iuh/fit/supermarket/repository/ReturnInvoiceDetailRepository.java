package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.ReturnInvoiceDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho ReturnInvoiceDetail
 */
@Repository
public interface ReturnInvoiceDetailRepository extends JpaRepository<ReturnInvoiceDetail, Integer> {

    /**
     * Tìm tất cả chi tiết trả hàng theo phiếu trả
     */
    List<ReturnInvoiceDetail> findByReturnInvoice_ReturnId(Integer returnId);
}
