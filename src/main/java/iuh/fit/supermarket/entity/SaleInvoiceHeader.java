package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.enums.InvoiceStatus;
import iuh.fit.supermarket.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity đại diện cho phần đầu hóa đơn bán hàng trong hệ thống
 */
@Entity
@Table(name = "sale_invoice_header")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleInvoiceHeader {

    /**
     * ID duy nhất của hóa đơn
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invoice_id")
    private Integer invoiceId;

    /**
     * Số hóa đơn (duy nhất)
     */
    @Column(name = "invoice_number", length = 50, nullable = false, unique = true)
    private String invoiceNumber;

    /**
     * Ngày lập hóa đơn
     */
    @Column(name = "invoice_date", nullable = false)
    private LocalDateTime invoiceDate = LocalDateTime.now();

    /**
     * Tổng tiền trước thuế và giảm giá
     */
    @Column(name = "subtotal", precision = 12, scale = 2, nullable = false)
    private BigDecimal subtotal = BigDecimal.ZERO;

    /**
     * Tổng tiền giảm giá
     */
    @Column(name = "total_discount", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalDiscount = BigDecimal.ZERO;

    /**
     * Tổng thuế
     */
    @Column(name = "total_tax", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalTax = BigDecimal.ZERO;

    /**
     * Tổng tiền cuối cùng
     */
    @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    /**
     * Trạng thái hóa đơn
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InvoiceStatus status = InvoiceStatus.UNPAID;

    /**
     * Phương thức thanh toán
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    /**
     * Số tiền đã thanh toán
     */
    @Column(name = "paid_amount", precision = 12, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    /**
     * Thời gian tạo
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Đơn hàng tương ứng
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    /**
     * Khách hàng
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    /**
     * Nhân viên lập hóa đơn
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * Danh sách chi tiết hóa đơn
     */
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SaleInvoiceDetail> invoiceDetails;

    /**
     * Danh sách phiếu trả hàng liên quan
     */
    @OneToMany(mappedBy = "originalInvoice", fetch = FetchType.LAZY)
    private List<ReturnInvoiceHeader> returnInvoices;
}
