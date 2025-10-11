package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.SaleInvoiceHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho quản lý hóa đơn bán hàng
 */
@Repository
public interface SaleInvoiceHeaderRepository extends JpaRepository<SaleInvoiceHeader, Integer> {
    
    /**
     * Tìm hóa đơn theo số hóa đơn
     */
    Optional<SaleInvoiceHeader> findByInvoiceNumber(String invoiceNumber);
    
    /**
     * Kiểm tra số hóa đơn đã tồn tại chưa
     */
    boolean existsByInvoiceNumber(String invoiceNumber);

    /**
     * Tìm danh sách hóa đơn theo order ID
     */
    List<SaleInvoiceHeader> findByOrder_OrderId(Long orderId);
}
