package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.SaleInvoiceHeader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    /**
     * Lấy danh sách hóa đơn với eager load các entity liên quan để tránh N+1
     * Eager load: order, invoiceDetails, invoiceDetails.productUnit, invoiceDetails.productUnit.product, invoiceDetails.productUnit.unit
     */
    @EntityGraph(attributePaths = {"order", "customer", "employee", "invoiceDetails", "invoiceDetails.productUnit", "invoiceDetails.productUnit.product", "invoiceDetails.productUnit.unit"})
    @Query("SELECT i FROM SaleInvoiceHeader i")
    Page<SaleInvoiceHeader> findAllWithDetails(Pageable pageable);

    /**
     * Tìm kiếm và lọc hoá đơn theo các tiêu chí:
     * - invoiceNumber: tìm kiếm mã hoá đơn (LIKE)
     * - customerName: tìm kiếm tên khách hàng (LIKE)
     * - fromDate: lọc từ ngày (>=)
     * - toDate: lọc đến ngày (<=)
     * - status: lọc theo trạng thái hoá đơn
     */
    @EntityGraph(attributePaths = {"order", "customer", "employee", "invoiceDetails", "invoiceDetails.productUnit", "invoiceDetails.productUnit.product", "invoiceDetails.productUnit.unit"})
    @Query("""
            SELECT i FROM SaleInvoiceHeader i
            WHERE (:invoiceNumber IS NULL OR LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :invoiceNumber, '%')))
            AND (:customerName IS NULL OR LOWER(i.customer.name) LIKE LOWER(CONCAT('%', :customerName, '%')))
            AND (:fromDate IS NULL OR CAST(i.invoiceDate AS DATE) >= :fromDate)
            AND (:toDate IS NULL OR CAST(i.invoiceDate AS DATE) <= :toDate)
            AND (:status IS NULL OR i.status = :status)
            """)
    Page<SaleInvoiceHeader> searchAndFilterInvoices(
            String invoiceNumber,
            String customerName,
            java.time.LocalDate fromDate,
            java.time.LocalDate toDate,
            iuh.fit.supermarket.enums.InvoiceStatus status,
            Pageable pageable
    );

    /**
     * Lấy hoá đơn theo ID với eager load đầy đủ thông tin
     * Eager load: order, customer, employee, invoiceDetails, invoiceDetails.productUnit và các quan hệ của nó
     */
    @EntityGraph(attributePaths = {"order", "customer", "employee", "invoiceDetails", "invoiceDetails.productUnit", "invoiceDetails.productUnit.product", "invoiceDetails.productUnit.unit"})
    @Query("SELECT i FROM SaleInvoiceHeader i WHERE i.invoiceId = :invoiceId")
    java.util.Optional<SaleInvoiceHeader> findByIdWithDetails(Integer invoiceId);
}
