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
@Table(name = "product_units")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUnit {

    /**
     * ID duy nhất của đơn vị
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Mã đơn vị (duy nhất)
     */
    @Column(name = "code", length = 50, unique = true)
    private String code;

    /**
     * Mã vạch của đơn vị
     */
    @Column(name = "barcode", length = 100)
    private String barcode;

    /**
     * Tên đơn vị (ví dụ: "lon", "lốc", "thùng")
     */
    @Column(name = "unit", length = 50, nullable = false)
    private String unit;

    /**
     * Giá bán cơ bản của đơn vị này
     */
    @Column(name = "base_price", precision = 18, scale = 2)
    private BigDecimal basePrice;

    /**
     * Tỷ lệ quy đổi so với đơn vị cơ bản
     * Ví dụ: lon=1, lốc=6, thùng=24
     */
    @Column(name = "conversion_value", nullable = false)
    private Integer conversionValue = 1;

    /**
     * Cho phép bán hay không
     */
    @Column(name = "allows_sale")
    private Boolean allowsSale = true;

    /**
     * Giá nhập gần nhất
     */
    @Column(name = "latest_purchase_price", precision = 18, scale = 2)
    private BigDecimal latestPurchasePrice;

    /**
     * ID đơn vị chính để clone
     */
    @Column(name = "master_unit_id_clone")
    private Long masterUnitIdClone;

    /**
     * Thời gian tạo
     */
    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    /**
     * Thời gian cập nhật
     */
    @UpdateTimestamp
    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

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
}
