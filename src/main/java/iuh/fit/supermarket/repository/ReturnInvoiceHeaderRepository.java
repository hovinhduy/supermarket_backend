package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.ReturnInvoiceHeader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho ReturnInvoiceHeader
 */
@Repository
public interface ReturnInvoiceHeaderRepository extends JpaRepository<ReturnInvoiceHeader, Integer> {

    /**
     * Tìm tất cả phiếu trả theo hóa đơn gốc
     */
    List<ReturnInvoiceHeader> findByOriginalInvoice_InvoiceId(Integer invoiceId);

    /**
     * Tìm phiếu trả theo mã phiếu trả
     */
    Optional<ReturnInvoiceHeader> findByReturnCode(String returnCode);

    /**
     * Tìm kiếm và lọc hóa đơn trả hàng theo các tiêu chí:
     * - returnCode: tìm kiếm mã trả hàng (LIKE)
     * - invoiceNumber: tìm kiếm mã hóa đơn gốc (LIKE)
     * - customerName: tìm kiếm tên khách hàng (LIKE)
     * - customerPhone: tìm kiếm số điện thoại khách hàng (LIKE)
     * - fromDate: lọc từ ngày (>=)
     * - toDate: lọc đến ngày (<=)
     * - employeeId: lọc theo nhân viên xử lý
     * - customerId: lọc theo khách hàng
     */
    @EntityGraph(attributePaths = {"originalInvoice", "customer", "employee"})
    @Query("""
            SELECT DISTINCT r FROM ReturnInvoiceHeader r
            LEFT JOIN r.originalInvoice inv
            LEFT JOIN r.customer c
            LEFT JOIN r.employee e
            WHERE (:returnCode IS NULL OR LOWER(r.returnCode) LIKE LOWER(CONCAT('%', :returnCode, '%')))
            AND (:invoiceNumber IS NULL OR LOWER(inv.invoiceNumber) LIKE LOWER(CONCAT('%', :invoiceNumber, '%')))
            AND (:customerName IS NULL OR (c IS NOT NULL AND LOWER(c.name) LIKE LOWER(CONCAT('%', :customerName, '%'))))
            AND (:customerPhone IS NULL OR (c IS NOT NULL AND LOWER(c.phone) LIKE LOWER(CONCAT('%', :customerPhone, '%'))))
            AND (:fromDate IS NULL OR CAST(r.returnDate AS DATE) >= :fromDate)
            AND (:toDate IS NULL OR CAST(r.returnDate AS DATE) <= :toDate)
            AND (:employeeId IS NULL OR e.employeeId = :employeeId)
            AND (:customerId IS NULL OR (c IS NOT NULL AND c.customerId = :customerId))
            ORDER BY r.returnDate DESC
            """)
    Page<ReturnInvoiceHeader> searchAndFilterReturns(
            @Param("returnCode") String returnCode,
            @Param("invoiceNumber") String invoiceNumber,
            @Param("customerName") String customerName,
            @Param("customerPhone") String customerPhone,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("employeeId") Integer employeeId,
            @Param("customerId") Integer customerId,
            Pageable pageable
    );

    /**
     * Lấy chi tiết phiếu trả với eager load đầy đủ thông tin
     */
    @EntityGraph(attributePaths = {"originalInvoice", "customer", "employee", "returnDetails", "returnDetails.productUnit", "returnDetails.productUnit.product", "returnDetails.productUnit.unit"})
    @Query("SELECT r FROM ReturnInvoiceHeader r WHERE r.returnId = :returnId")
    Optional<ReturnInvoiceHeader> findByIdWithDetails(@Param("returnId") Integer returnId);
}
