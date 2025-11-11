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
     * - searchKeyword: tìm kiếm trong mã trả hàng, mã hóa đơn gốc, tên khách hàng, số điện thoại khách hàng (LIKE)
     * - fromDate: lọc từ ngày (>=)
     * - toDate: lọc đến ngày (<=)
     * - employeeId: lọc theo nhân viên xử lý
     * - customerId: lọc theo khách hàng
     * - productUnitId: lọc theo sản phẩm đơn vị trong chi tiết phiếu trả
     */
    @EntityGraph(attributePaths = {"originalInvoice", "customer", "employee"})
    @Query("""
            SELECT DISTINCT r FROM ReturnInvoiceHeader r
            LEFT JOIN r.originalInvoice inv
            LEFT JOIN r.customer c
            LEFT JOIN c.user cu
            LEFT JOIN r.employee e
            LEFT JOIN r.returnDetails rd
            WHERE (:searchKeyword IS NULL OR
                   LOWER(r.returnCode) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR
                   LOWER(inv.invoiceNumber) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR
                   (cu IS NOT NULL AND LOWER(cu.name) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))) OR
                   (cu IS NOT NULL AND LOWER(cu.phone) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))))
            AND (:fromDate IS NULL OR CAST(r.returnDate AS DATE) >= :fromDate)
            AND (:toDate IS NULL OR CAST(r.returnDate AS DATE) <= :toDate)
            AND (:employeeId IS NULL OR e.employeeId = :employeeId)
            AND (:customerId IS NULL OR (c IS NOT NULL AND c.customerId = :customerId))
            AND (:productUnitId IS NULL OR rd.productUnit.id = :productUnitId)
            ORDER BY r.returnDate DESC
            """)
    Page<ReturnInvoiceHeader> searchAndFilterReturns(
            @Param("searchKeyword") String searchKeyword,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("employeeId") Integer employeeId,
            @Param("customerId") Integer customerId,
            @Param("productUnitId") Integer productUnitId,
            Pageable pageable
    );

    /**
     * Lấy chi tiết phiếu trả với eager load đầy đủ thông tin
     */
    @EntityGraph(attributePaths = {"originalInvoice", "customer", "employee", "returnDetails", "returnDetails.productUnit", "returnDetails.productUnit.product", "returnDetails.productUnit.unit"})
    @Query("SELECT r FROM ReturnInvoiceHeader r WHERE r.returnId = :returnId")
    Optional<ReturnInvoiceHeader> findByIdWithDetails(@Param("returnId") Integer returnId);

    /**
     * Lấy dữ liệu báo cáo trả hàng chi tiết theo khoảng thời gian
     * Trả về: hóa đơn mua, ngày mua, hóa đơn trả, ngày trả, nhóm SP, mã SP, tên SP, đơn vị, số lượng, đơn giá, thành tiền
     *
     * @param fromDate từ ngày
     * @param toDate đến ngày
     * @return danh sách Object[] chứa thông tin báo cáo
     */
    @Query("""
            SELECT
                inv.invoiceNumber,
                inv.invoiceDate,
                rh.returnCode,
                rh.returnDate,
                cat.name,
                p.code,
                p.name,
                u.name,
                rd.quantity,
                rd.priceAtReturn,
                rd.refundAmount
            FROM ReturnInvoiceHeader rh
            JOIN rh.originalInvoice inv
            JOIN rh.returnDetails rd
            JOIN rd.productUnit pu
            JOIN pu.product p
            JOIN pu.unit u
            LEFT JOIN p.category cat
            WHERE DATE(rh.returnDate) >= :fromDate
            AND DATE(rh.returnDate) <= :toDate
            ORDER BY rh.returnDate DESC, rh.returnCode
            """)
    List<Object[]> getReturnReportRaw(
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );
}
