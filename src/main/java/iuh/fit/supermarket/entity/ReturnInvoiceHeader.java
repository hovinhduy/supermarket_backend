package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.enums.ReturnStatus;
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
 * Entity đại diện cho phần đầu hóa đơn trả hàng trong hệ thống
 */
@Entity
@Table(name = "return_invoice_header")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnInvoiceHeader {

    /**
     * ID duy nhất của phiếu trả hàng
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "return_id")
    private Integer returnId;

    /**
     * Mã phiếu trả hàng (duy nhất)
     */
    @Column(name = "return_code", length = 50, nullable = false, unique = true)
    private String returnCode;

    /**
     * Ngày trả hàng
     */
    @Column(name = "return_date", nullable = false)
    private LocalDateTime returnDate = LocalDateTime.now();

    /**
     * Tổng giá trị các sản phẩm được trả lại
     */
    @Column(name = "total_refund_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalRefundAmount = BigDecimal.ZERO;

    /**
     * Số tiền khuyến mãi bị thu hồi
     */
    @Column(name = "reclaimed_discount_amount", precision = 12, scale = 2)
    private BigDecimal reclaimedDiscountAmount = BigDecimal.ZERO;

    /**
     * Số tiền thực tế trả cho khách
     */
    @Column(name = "final_refund_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal finalRefundAmount;

    /**
     * Ghi chú lý do trả hàng
     */
    @Column(name = "reason_note", columnDefinition = "TEXT")
    private String reasonNote;

    /**
     * Trạng thái phiếu trả hàng
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReturnStatus status = ReturnStatus.PENDING;

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
     * Hóa đơn gốc
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_invoice_id", nullable = false)
    private SaleInvoiceHeader originalInvoice;

    /**
     * Khách hàng
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    /**
     * Nhân viên xử lý trả hàng
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * Danh sách chi tiết trả hàng
     */
    @OneToMany(mappedBy = "returnInvoice", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ReturnInvoiceDetail> returnDetails;
}
