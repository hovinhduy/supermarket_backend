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
     * Tìm kiếm và lọc hoá đơn theo các tiêu chí:
     * - searchKeyword: tìm kiếm trong mã hoá đơn và số điện thoại khách hàng (LIKE)
     * - fromDate: lọc từ ngày (>=)
     * - toDate: lọc đến ngày (<=)
     * - status: lọc theo trạng thái hoá đơn
     * - employeeId: lọc theo nhân viên
     * - customerId: lọc theo khách hàng
     * - productUnitId: lọc theo sản phẩm đơn vị trong chi tiết hoá đơn
     */
    @EntityGraph(attributePaths = {"order", "customer", "employee", "invoiceDetails", "invoiceDetails.productUnit", "invoiceDetails.productUnit.product", "invoiceDetails.productUnit.unit"})
    @Query("""
            SELECT DISTINCT i FROM SaleInvoiceHeader i
            LEFT JOIN i.customer c
            LEFT JOIN i.employee e
            LEFT JOIN i.invoiceDetails d
            WHERE (:searchKeyword IS NULL OR 
                   LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR
                   (c IS NOT NULL AND LOWER(c.phone) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))))
            AND (:fromDate IS NULL OR CAST(i.invoiceDate AS DATE) >= :fromDate)
            AND (:toDate IS NULL OR CAST(i.invoiceDate AS DATE) <= :toDate)
            AND (:status IS NULL OR i.status = :status)
            AND (:employeeId IS NULL OR e.employeeId = :employeeId)
            AND (:customerId IS NULL OR (c IS NOT NULL AND c.customerId = :customerId))
            AND (:productUnitId IS NULL OR d.productUnit.id = :productUnitId)
            """)
    Page<SaleInvoiceHeader> searchAndFilterInvoices(
            String searchKeyword,
            java.time.LocalDate fromDate,
            java.time.LocalDate toDate,
            iuh.fit.supermarket.enums.InvoiceStatus status,
            Integer employeeId,
            Integer customerId,
            Integer productUnitId,
            Pageable pageable
    );

    /**
     * Lấy hoá đơn theo ID với eager load đầy đủ thông tin
     * Eager load: order, customer, employee, invoiceDetails, invoiceDetails.productUnit và các quan hệ của nó
     */
    @EntityGraph(attributePaths = {"order", "customer", "employee", "invoiceDetails", "invoiceDetails.productUnit", "invoiceDetails.productUnit.product", "invoiceDetails.productUnit.unit"})
    @Query("SELECT i FROM SaleInvoiceHeader i WHERE i.invoiceId = :invoiceId")
    java.util.Optional<SaleInvoiceHeader> findByIdWithDetails(Integer invoiceId);

    /**
     * Lấy dữ liệu báo cáo doanh số bán hàng theo ngày
     * Nhóm theo nhân viên và ngày, chỉ lấy hóa đơn PAID
     */
    @Query("""
            SELECT e.employeeCode, e.name, MIN(i.invoiceDate),
                   COALESCE(SUM(i.totalDiscount), 0.0),
                   COALESCE(SUM(i.subtotal), 0.0),
                   COALESCE(SUM(i.totalAmount), 0.0)
            FROM SaleInvoiceHeader i
            JOIN i.employee e
            WHERE i.status = 'PAID'
            AND FUNCTION('DATE', i.invoiceDate) >= :fromDate
            AND FUNCTION('DATE', i.invoiceDate) <= :toDate
            AND (:employeeId IS NULL OR e.employeeId = :employeeId)
            GROUP BY e.employeeCode, e.name, FUNCTION('DATE', i.invoiceDate)
            ORDER BY e.employeeCode, FUNCTION('DATE', i.invoiceDate)
            """)
    List<Object[]> getSalesDailyReportRaw(
            java.time.LocalDate fromDate,
            java.time.LocalDate toDate,
            Integer employeeId
    );
}
