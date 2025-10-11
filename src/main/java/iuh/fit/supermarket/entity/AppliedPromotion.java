package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity lưu thông tin khuyến mãi đã áp dụng cho từng dòng hóa đơn
 */
@Entity
@Table(name = "applied_promotions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppliedPromotion {

    /**
     * ID duy nhất
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "applied_promotion_id")
    private Long appliedPromotionId;

    /**
     * Mã khuyến mãi
     */
    @Column(name = "promotion_id", length = 50)
    private String promotionId;

    /**
     * Tên khuyến mãi
     */
    @Column(name = "promotion_name", length = 255)
    private String promotionName;

    /**
     * ID chi tiết khuyến mãi
     */
    @Column(name = "promotion_detail_id")
    private Long promotionDetailId;

    /**
     * Mô tả khuyến mãi
     */
    @Column(name = "promotion_summary", columnDefinition = "TEXT")
    private String promotionSummary;

    /**
     * Loại giảm giá (percentage, fixed_amount)
     */
    @Column(name = "discount_type", length = 50)
    private String discountType;

    /**
     * Giá trị giảm giá
     */
    @Column(name = "discount_value", precision = 12, scale = 2)
    private BigDecimal discountValue;

    /**
     * ID dòng sản phẩm gốc (cho trường hợp item tặng)
     */
    @Column(name = "source_line_item_id")
    private Long sourceLineItemId;

    /**
     * Thời gian tạo
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Chi tiết hóa đơn áp dụng khuyến mãi này
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_detail_id", nullable = false)
    private SaleInvoiceDetail invoiceDetail;
}
