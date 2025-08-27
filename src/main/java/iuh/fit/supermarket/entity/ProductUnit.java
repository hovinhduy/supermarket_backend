package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.enums.WeightUnit;
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
 * Entity đại diện cho đơn vị đóng gói sản phẩm trong hệ thống
 */
@Entity
@Table(name = "product_units", uniqueConstraints = @UniqueConstraint(columnNames = { "product_id", "name" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUnit {

    /**
     * ID duy nhất của đơn vị
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "unit_id")
    private Integer unitId;

    /**
     * Tên đơn vị (ví dụ: "Lon", "Chai 390ml", "Lốc 6", "Thùng 24")
     */
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    /**
     * Hệ số quy đổi so với đơn vị cơ bản
     * Ví dụ: Lon=1, Lốc=6, Thùng=24
     */
    @Column(name = "conversion_factor", nullable = false)
    private Integer conversionFactor = 1;

    /**
     * Trọng lượng (bao gồm bao bì)
     */
    @Column(name = "weight", precision = 10, scale = 3)
    private BigDecimal weight;

    /**
     * Đơn vị trọng lượng
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "weight_unit")
    private WeightUnit weightUnit;

    /**
     * Chiều dài (cm)
     */
    @Column(name = "length", precision = 8, scale = 2)
    private BigDecimal length;

    /**
     * Chiều rộng (cm)
     */
    @Column(name = "width", precision = 8, scale = 2)
    private BigDecimal width;

    /**
     * Chiều cao (cm)
     */
    @Column(name = "height", precision = 8, scale = 2)
    private BigDecimal height;

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
     * Sản phẩm mà đơn vị này thuộc về
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Danh sách biến thể sử dụng đơn vị này
     */
    @OneToMany(mappedBy = "unit", fetch = FetchType.LAZY)
    private List<ProductVariant> variants;

    /**
     * Danh sách sản phẩm sử dụng đơn vị này làm đơn vị cơ bản
     */
    @OneToMany(mappedBy = "baseUnit", fetch = FetchType.LAZY)
    private List<Product> productsAsBaseUnit;
}
