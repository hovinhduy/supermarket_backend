package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity đại diện cho sản phẩm trong hệ thống
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    /**
     * ID duy nhất của sản phẩm
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    /**
     * Mã sản phẩm (duy nhất)
     */
    @Column(name = "product_code", length = 100, nullable = false, unique = true)
    private String productCode;

    /**
     * Tên sản phẩm
     */
    @Column(name = "name", length = 500, nullable = false)
    private String name;

    /**
     * Mô tả sản phẩm
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Mô tả ngắn
     */
    @Column(name = "short_description", length = 1000)
    private String shortDescription;

    /**
     * Trạng thái sản phẩm
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ProductStatus status = ProductStatus.ACTIVE;

    /**
     * Sản phẩm nổi bật
     */
    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    /**
     * Trạng thái xóa mềm
     */
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

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
     * Thương hiệu của sản phẩm
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    /**
     * Danh mục của sản phẩm
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /**
     * Đơn vị cơ bản để quản lý tồn kho
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_unit_id")
    private ProductUnit baseUnit;

    /**
     * Danh sách đơn vị đóng gói của sản phẩm
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductUnit> units;

    /**
     * Danh sách biến thể của sản phẩm
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductVariant> variants;

    /**
     * Danh sách hình ảnh của sản phẩm
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductImage> images;
}
