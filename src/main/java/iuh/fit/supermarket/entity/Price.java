package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity đại diện cho giá sản phẩm trong hệ thống
 * Quản lý giá nâng cao với nhiều bảng giá và thời gian hiệu lực
 */
@Entity
@Table(name = "Prices")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Price {

    /**
     * ID duy nhất của giá
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "price_id")
    private Integer priceId;

    /**
     * Giá sản phẩm
     */
    @Column(name = "price", nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    /**
     * Ngày bắt đầu hiệu lực
     */
    @Column(name = "start_date")
    private LocalDate startDate;

    /**
     * Ngày kết thúc hiệu lực
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Biến thể sản phẩm
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    /**
     * Bảng giá
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_list_id", nullable = false)
    private PriceList priceList;
}
