package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho chi tiết phiếu nhập hàng trong hệ thống
 */
@Entity
@Table(name = "import_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportDetail {

    /**
     * ID duy nhất của chi tiết nhập hàng
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "import_detail_id")
    private Integer importDetailId;

    /**
     * Số lượng nhập
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * Giá nhập trên một đơn vị
     */
    @Column(name = "cost_per_unit", precision = 10, scale = 2, nullable = false)
    private BigDecimal costPerUnit;

    /**
     * Ngày hết hạn
     */
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    /**
     * Thời gian tạo
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Phiếu nhập
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_id", nullable = false)
    private Import importRecord;

    /**
     * Biến thể sản phẩm được nhập
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    /**
     * Tính tổng giá trị nhập cho dòng này
     * 
     * @return tổng giá trị (số lượng * giá đơn vị)
     */
    public BigDecimal getTotalValue() {
        return costPerUnit.multiply(BigDecimal.valueOf(quantity));
    }
}
