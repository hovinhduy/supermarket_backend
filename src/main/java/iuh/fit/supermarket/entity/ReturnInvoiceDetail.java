package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.enums.MovementReason;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho chi tiết hóa đơn trả hàng trong hệ thống
 */
@Entity
@Table(name = "return_invoice_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturnInvoiceDetail {

    /**
     * ID duy nhất của chi tiết trả hàng
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "return_detail_id")
    private Integer returnDetailId;

    /**
     * Số lượng sản phẩm được trả lại
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * Đơn giá tại thời điểm trả hàng
     */
    @Column(name = "price_at_return", precision = 12, scale = 2, nullable = false)
    private BigDecimal priceAtReturn;

    /**
     * Tổng tiền hoàn lại cho dòng sản phẩm này
     */
    @Column(name = "refund_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal refundAmount;

    /**
     * Lý do trả hàng cho từng sản phẩm
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "reason")
    private MovementReason reason;

    /**
     * Thời gian tạo
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Phiếu trả hàng chứa chi tiết này
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_id", nullable = false)
    private ReturnInvoiceHeader returnInvoice;

    /**
     * Biến thể sản phẩm được trả
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;
}
