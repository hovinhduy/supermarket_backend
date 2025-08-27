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
    private Integer orderDetailId;

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
     * Biến thể sản phẩm
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

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
