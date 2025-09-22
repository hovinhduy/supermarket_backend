package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho chi tiết kiểm kê kho trong hệ thống
 */
@Entity
@Table(name = "stocktake_details", uniqueConstraints = @UniqueConstraint(columnNames = { "stocktake_id",
        "variant_id" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StocktakeDetail {

    /**
     * ID duy nhất của chi tiết kiểm kê
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stocktake_detail_id")
    private Integer stocktakeDetailId;

    /**
     * Số lượng tồn kho theo hệ thống tại thời điểm tạo phiếu
     */
    @Column(name = "quantity_expected", nullable = false)
    private Integer quantityExpected;

    /**
     * Số lượng thực tế đếm được
     */
    @Column(name = "quantity_counted", nullable = false)
    private Integer quantityCounted;

    /**
     * Chênh lệch (counted - expected)
     */
    @Column(name = "quantity_difference", nullable = false)
    private Integer quantityDifference;

    /**
     * Số lượng tăng
     */
    @Column(name = "quantity_increase")
    private Integer quantityIncrease;

    /**
     * Số lượng giảm
     */
    @Column(name = "quantity_decrease")
    private Integer quantityDecrease;

    /**
     * Ghi chú lý do chênh lệch
     */
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    /**
     * Thời gian tạo
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Phiếu kiểm kê chứa chi tiết này
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stocktake_id", nullable = false)
    private Stocktake stocktake;

    /**
     * Biến thể sản phẩm được kiểm kê
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    /**
     * Tính toán chênh lệch tự động
     */
    @PrePersist
    @PreUpdate
    public void calculateDifference() {
        this.quantityDifference = this.quantityCounted - this.quantityExpected;
    }
}
