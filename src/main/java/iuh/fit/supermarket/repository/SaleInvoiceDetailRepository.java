package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.SaleInvoiceDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository cho quản lý chi tiết hóa đơn bán hàng
 */
@Repository
public interface SaleInvoiceDetailRepository extends JpaRepository<SaleInvoiceDetail, Integer> {
}
