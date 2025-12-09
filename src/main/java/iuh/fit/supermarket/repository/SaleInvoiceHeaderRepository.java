package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.SaleInvoiceHeader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
     * Kiểm tra xem đơn hàng đã có hóa đơn chưa
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM SaleInvoiceHeader s WHERE s.order.orderId = :orderId")
    boolean existsByOrderId(@Param("orderId") Long orderId);

    /**
     * Lấy số hóa đơn cuối cùng trong tháng
     * @param yearMonth chuỗi năm tháng format yyyyMM
     * @return danh sách số hóa đơn sắp xếp giảm dần
     */
    @Query("SELECT s.invoiceNumber FROM SaleInvoiceHeader s WHERE s.invoiceNumber LIKE CONCAT('INV', :yearMonth, '%') ORDER BY s.invoiceNumber DESC")
    List<String> findLastInvoiceNumberByMonth(@Param("yearMonth") String yearMonth);

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
            LEFT JOIN c.user cu
            LEFT JOIN i.employee e
            LEFT JOIN i.invoiceDetails d
            WHERE (:searchKeyword IS NULL OR
                   LOWER(i.invoiceNumber) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR
                   (cu IS NOT NULL AND LOWER(cu.phone) LIKE LOWER(CONCAT('%', :searchKeyword, '%'))))
            AND (:fromDate IS NULL OR CAST(i.invoiceDate AS DATE) >= :fromDate)
            AND (:toDate IS NULL OR CAST(i.invoiceDate AS DATE) <= :toDate)
            AND (:status IS NULL OR i.status = :status)
            AND (:employeeId IS NULL OR e.employeeId = :employeeId)
            AND (:customerId IS NULL OR (c IS NOT NULL AND c.customerId = :customerId))
            AND (:productUnitId IS NULL OR d.productUnit.id = :productUnitId)
            ORDER BY i.invoiceDate DESC
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
            SELECT e.employeeCode, u.name, MIN(i.invoiceDate),
                   COALESCE(SUM(i.totalDiscount), 0.0),
                   COALESCE(SUM(i.subtotal), 0.0),
                   COALESCE(SUM(i.totalAmount), 0.0)
            FROM SaleInvoiceHeader i
            JOIN i.employee e
            JOIN e.user u
            WHERE i.status = 'PAID'
            AND FUNCTION('DATE', i.invoiceDate) >= :fromDate
            AND FUNCTION('DATE', i.invoiceDate) <= :toDate
            AND (:employeeId IS NULL OR e.employeeId = :employeeId)
            GROUP BY e.employeeCode, u.name, FUNCTION('DATE', i.invoiceDate)
            ORDER BY e.employeeCode, FUNCTION('DATE', i.invoiceDate)
            """)
    List<Object[]> getSalesDailyReportRaw(
            java.time.LocalDate fromDate,
            java.time.LocalDate toDate,
            Integer employeeId
    );

    /**
     * Lấy dữ liệu báo cáo doanh số theo khách hàng và nhóm sản phẩm
     * Chỉ tính các hóa đơn đã thanh toán (PAID)
     * Nhóm theo khách hàng và danh mục sản phẩm
     *
     * @param fromDate từ ngày (bao gồm cả ngày này)
     * @param toDate đến ngày (bao gồm cả ngày này)
     * @param customerId ID khách hàng (null để lấy tất cả khách hàng)
     * @return danh sách dữ liệu doanh số theo khách hàng và nhóm sản phẩm
     */
    @Query("""
            SELECT new iuh.fit.supermarket.dto.report.CustomerCategorySalesProjection(
                c.customerId,
                c.customerCode,
                u.name,
                c.address,
                CAST(c.customerType AS string),
                cat.name,
                SUM(d.unitPrice * d.quantity),
                SUM(d.discountAmount),
                SUM(d.lineTotal)
            )
            FROM SaleInvoiceHeader i
            JOIN i.customer c
            JOIN c.user u
            JOIN i.invoiceDetails d
            JOIN d.productUnit pu
            JOIN pu.product p
            LEFT JOIN p.category cat
            WHERE i.status = 'PAID'
            AND FUNCTION('DATE', i.invoiceDate) >= :fromDate
            AND FUNCTION('DATE', i.invoiceDate) <= :toDate
            AND (:customerId IS NULL OR c.customerId = :customerId)
            GROUP BY c.customerId, c.customerCode, u.name, c.address, c.customerType, cat.name
            ORDER BY c.customerCode, cat.name
            """)
    List<iuh.fit.supermarket.dto.report.CustomerCategorySalesProjection> findCustomerSalesReport(
            @Param("fromDate") java.time.LocalDate fromDate,
            @Param("toDate") java.time.LocalDate toDate,
            @Param("customerId") Integer customerId
    );

    /**
     * Đếm số lượng hóa đơn đã thanh toán trong khoảng thời gian (dashboard)
     *
     * @param fromDate từ ngày
     * @param toDate   đến ngày
     * @return số lượng hóa đơn đã thanh toán
     */
    @Query("SELECT COUNT(i) FROM SaleInvoiceHeader i WHERE " +
                    "i.status = 'PAID' " +
                    "AND CAST(i.invoiceDate AS DATE) >= :fromDate " +
                    "AND CAST(i.invoiceDate AS DATE) <= :toDate")
    long countPaidInvoicesByDateRange(
                    @Param("fromDate") java.time.LocalDate fromDate,
                    @Param("toDate") java.time.LocalDate toDate);

    /**
     * Tính tổng doanh thu từ hóa đơn đã thanh toán trong khoảng thời gian (dashboard)
     *
     * @param fromDate từ ngày
     * @param toDate   đến ngày
     * @return tổng doanh thu
     */
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM SaleInvoiceHeader i WHERE " +
                    "i.status = 'PAID' " +
                    "AND CAST(i.invoiceDate AS DATE) >= :fromDate " +
                    "AND CAST(i.invoiceDate AS DATE) <= :toDate")
    java.math.BigDecimal sumPaidInvoicesTotalByDateRange(
                    @Param("fromDate") java.time.LocalDate fromDate,
                    @Param("toDate") java.time.LocalDate toDate);

    /**
     * Lấy doanh thu và số lượng hóa đơn theo giờ trong ngày (dashboard chart)
     * Trả về: [giờ (0-23), tổng doanh thu, số lượng hóa đơn]
     *
     * @param date ngày cần xem
     * @return danh sách [hour, totalRevenue, invoiceCount]
     */
    @Query("SELECT FUNCTION('HOUR', i.invoiceDate) as hour, " +
           "COALESCE(SUM(i.totalAmount), 0) as totalRevenue, " +
           "COUNT(i) as invoiceCount " +
           "FROM SaleInvoiceHeader i " +
           "WHERE i.status = 'PAID' " +
           "AND CAST(i.invoiceDate AS DATE) = :date " +
           "GROUP BY FUNCTION('HOUR', i.invoiceDate) " +
           "ORDER BY FUNCTION('HOUR', i.invoiceDate)")
    List<Object[]> getRevenueByHourOfDay(@Param("date") java.time.LocalDate date);

    /**
     * Lấy doanh thu và số lượng hóa đơn theo ngày trong tuần (dashboard chart)
     * Trả về: [ngày, tổng doanh thu, số lượng hóa đơn]
     *
     * @param fromDate từ ngày (thứ 2)
     * @param toDate đến ngày (hiện tại)
     * @return danh sách [date, totalRevenue, invoiceCount]
     */
    @Query("SELECT CAST(i.invoiceDate AS DATE) as date, " +
           "COALESCE(SUM(i.totalAmount), 0) as totalRevenue, " +
           "COUNT(i) as invoiceCount " +
           "FROM SaleInvoiceHeader i " +
           "WHERE i.status = 'PAID' " +
           "AND CAST(i.invoiceDate AS DATE) >= :fromDate " +
           "AND CAST(i.invoiceDate AS DATE) <= :toDate " +
           "GROUP BY CAST(i.invoiceDate AS DATE) " +
           "ORDER BY CAST(i.invoiceDate AS DATE)")
    List<Object[]> getRevenueByDayOfWeek(
            @Param("fromDate") java.time.LocalDate fromDate,
            @Param("toDate") java.time.LocalDate toDate);

    /**
     * Lấy doanh thu và số lượng hóa đơn theo ngày trong tháng (dashboard chart)
     * Trả về: [ngày, tổng doanh thu, số lượng hóa đơn]
     *
     * @param fromDate từ ngày đầu tháng
     * @param toDate đến ngày hiện tại
     * @return danh sách [date, totalRevenue, invoiceCount]
     */
    @Query("SELECT CAST(i.invoiceDate AS DATE) as date, " +
           "COALESCE(SUM(i.totalAmount), 0) as totalRevenue, " +
           "COUNT(i) as invoiceCount " +
           "FROM SaleInvoiceHeader i " +
           "WHERE i.status = 'PAID' " +
           "AND CAST(i.invoiceDate AS DATE) >= :fromDate " +
           "AND CAST(i.invoiceDate AS DATE) <= :toDate " +
           "GROUP BY CAST(i.invoiceDate AS DATE) " +
           "ORDER BY CAST(i.invoiceDate AS DATE)")
    List<Object[]> getRevenueByDayOfMonth(
            @Param("fromDate") java.time.LocalDate fromDate,
            @Param("toDate") java.time.LocalDate toDate);

    /**
     * Lấy doanh thu và số lượng hóa đơn theo tháng trong năm (dashboard chart)
     * Trả về: [tháng (1-12), tổng doanh thu, số lượng hóa đơn]
     *
     * @param fromDate từ ngày đầu năm
     * @param toDate đến ngày hiện tại
     * @return danh sách [month, totalRevenue, invoiceCount]
     */
    @Query("SELECT FUNCTION('MONTH', i.invoiceDate) as month, " +
           "COALESCE(SUM(i.totalAmount), 0) as totalRevenue, " +
           "COUNT(i) as invoiceCount " +
           "FROM SaleInvoiceHeader i " +
           "WHERE i.status = 'PAID' " +
           "AND CAST(i.invoiceDate AS DATE) >= :fromDate " +
           "AND CAST(i.invoiceDate AS DATE) <= :toDate " +
           "GROUP BY FUNCTION('MONTH', i.invoiceDate) " +
           "ORDER BY FUNCTION('MONTH', i.invoiceDate)")
    List<Object[]> getRevenueByMonthOfYear(
            @Param("fromDate") java.time.LocalDate fromDate,
            @Param("toDate") java.time.LocalDate toDate);
}
