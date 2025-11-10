package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho chi tiết đơn hàng trong hệ thống
 */
@Entity
@Table(name = "order_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail {

    /**
     * ID duy nhất của chi tiết đơn hàng
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id")
    private Long orderDetailId;

    /**
     * Số lượng sản phẩm
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * Giá tại thời điểm mua
     */
    @Column(name = "price_at_purchase", precision = 10, scale = 2, nullable = false)
    private BigDecimal priceAtPurchase;

    /**
     * Số tiền giảm giá
     */
    @Column(name = "discount", precision = 10, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    /**
     * Tên khuyến mãi
     */
    @Column(name = "promotion_name", length = 255)
    private String promotionName;

    /**
     * ID của promotion line
     */
    @Column(name = "promotion_line_id")
    private Long promotionLineId;

    /**
     * ID chi tiết khuyến mãi (promotion_detail_id)
     */
    @Column(name = "promotion_detail_id")
    private Long promotionDetailId;

    /**
     * Mô tả chi tiết khuyến mãi
     */
    @Column(name = "promotion_summary", columnDefinition = "TEXT")
    private String promotionSummary;

    /**
     * Loại giảm giá (percentage, fixed)
     */
    @Column(name = "discount_type", length = 50)
    private String discountType;

    /**
     * Giá trị giảm giá
     */
    @Column(name = "discount_value", precision = 12, scale = 2)
    private BigDecimal discountValue;

    /**
     * ID dòng sản phẩm gốc (cho trường hợp item tặng, trỏ về buy product)
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
     * Đơn hàng chứa chi tiết này
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Đơn vị sản phẩm
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_unit_id", nullable = false)
    private ProductUnit productUnit;

    /**
     * Tính tổng tiền cho dòng này
     * 
     * @return tổng tiền (số lượng * giá - giảm giá)
     */
    public BigDecimal getLineTotal() {
        BigDecimal subtotal = priceAtPurchase.multiply(BigDecimal.valueOf(quantity));
        return subtotal.subtract(discount);
    }
}
