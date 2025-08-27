package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.enums.PriceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho lịch sử giá sản phẩm trong hệ thống
 */
@Entity
@Table(name = "price_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistory {

    /**
     * ID duy nhất của lịch sử giá
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_id")
    private Integer priceId;

    /**
     * Giá thường
     */
    @Column(name = "regular_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal regularPrice;

    /**
     * Giá khuyến mãi
     */
    @Column(name = "sale_price", precision = 12, scale = 2)
    private BigDecimal salePrice;

    /**
     * Giá vốn
     */
    @Column(name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice;

    /**
     * Thời gian bắt đầu hiệu lực
     */
    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    /**
     * Thời gian kết thúc hiệu lực (NULL = giá hiện tại)
     */
    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;

    /**
     * Loại giá
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "price_type")
    private PriceType priceType = PriceType.REGULAR;

    /**
     * Thời gian tạo
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Biến thể sản phẩm
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    /**
     * Nhân viên tạo lịch sử giá
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Employee createdBy;
}
