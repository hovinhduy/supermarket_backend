package iuh.fit.supermarket.repository;

import iuh.fit.supermarket.entity.SaleInvoiceDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho quản lý chi tiết hóa đơn bán hàng
 */
@Repository
public interface SaleInvoiceDetailRepository extends JpaRepository<SaleInvoiceDetail, Integer> {
    
    /**
     * Tìm tất cả chi tiết hóa đơn theo invoice ID
     */
    List<SaleInvoiceDetail> findByInvoice_InvoiceId(Integer invoiceId);

    /**
     * Lấy top 5 sản phẩm bán chạy nhất theo doanh thu
     * Trả về: [productUnitId, productId, productName, unitName, barcode, totalQuantity, totalRevenue]
     *
     * @param fromDate từ ngày
     * @param toDate   đến ngày
     * @return danh sách top 5 sản phẩm
     */
    @Query("""
            SELECT d.productUnit.id,
                   d.productUnit.product.id,
                   d.productUnit.product.name,
                   d.productUnit.unit.name,
                   d.productUnit.barcode,
                   SUM(d.quantity),
                   SUM(d.lineTotal)
            FROM SaleInvoiceDetail d
            JOIN d.invoice i
            WHERE i.status = 'PAID'
            AND CAST(i.invoiceDate AS DATE) >= :fromDate
            AND CAST(i.invoiceDate AS DATE) <= :toDate
            GROUP BY d.productUnit.id, d.productUnit.product.id, 
                     d.productUnit.product.name, d.productUnit.unit.name, d.productUnit.barcode
            ORDER BY SUM(d.lineTotal) DESC
            """)
    List<Object[]> findTop5ProductsByRevenue(
            @Param("fromDate") java.time.LocalDate fromDate,
            @Param("toDate") java.time.LocalDate toDate,
            org.springframework.data.domain.Pageable pageable);

    /**
     * Lấy top 5 sản phẩm bán chạy nhất theo số lượng
     * Trả về: [productUnitId, productId, productName, unitName, barcode, totalQuantity, totalRevenue]
     *
     * @param fromDate từ ngày
     * @param toDate   đến ngày
     * @return danh sách top 5 sản phẩm
     */
    @Query("""
            SELECT d.productUnit.id,
                   d.productUnit.product.id,
                   d.productUnit.product.name,
                   d.productUnit.unit.name,
                   d.productUnit.barcode,
                   SUM(d.quantity),
                   SUM(d.lineTotal)
            FROM SaleInvoiceDetail d
            JOIN d.invoice i
            WHERE i.status = 'PAID'
            AND CAST(i.invoiceDate AS DATE) >= :fromDate
            AND CAST(i.invoiceDate AS DATE) <= :toDate
            GROUP BY d.productUnit.id, d.productUnit.product.id, 
                     d.productUnit.product.name, d.productUnit.unit.name, d.productUnit.barcode
            ORDER BY SUM(d.quantity) DESC
            """)
    List<Object[]> findTop5ProductsByQuantity(
            @Param("fromDate") java.time.LocalDate fromDate,
            @Param("toDate") java.time.LocalDate toDate,
            org.springframework.data.domain.Pageable pageable);
}
