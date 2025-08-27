package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.enums.TaxType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho chi tiết hóa đơn bán hàng trong hệ thống
 */
@Entity
@Table(name = "sale_invoice_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleInvoiceDetail {

    /**
     * ID duy nhất của chi tiết hóa đơn
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_detail_id")
    private Integer invoiceDetailId;

    /**
     * Số lượng sản phẩm
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * Đơn giá
     */
    @Column(name = "unit_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    /**
     * Số tiền giảm giá
     */
    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /**
     * Thành tiền trước thuế
     */
    @Column(name = "line_total", precision = 12, scale = 2, nullable = false)
    private BigDecimal lineTotal;

    /**
     * Loại thuế
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "tax_type")
    private TaxType taxType = TaxType.VAT_10;

    /**
     * Số tiền thuế
     */
    @Column(name = "tax_amount", precision = 12, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    /**
     * Thành tiền sau thuế
     */
    @Column(name = "line_total_with_tax", precision = 12, scale = 2, nullable = false)
    private BigDecimal lineTotalWithTax;

    /**
     * Thời gian tạo
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Hóa đơn chứa chi tiết này
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private SaleInvoiceHeader invoice;

    /**
     * Biến thể sản phẩm
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;
}
