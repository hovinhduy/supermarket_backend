package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho mối quan hệ giữa ProductUnit và ProductImage
 * Cho phép ProductUnit chọn các hình ảnh cụ thể từ danh sách hình ảnh của
 * Product
 */
@Entity
@Table(name = "product_unit_images", uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_unit_image", columnNames = { "product_unit_id", "product_image_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUnitImage {

    /**
     * ID duy nhất của mapping
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Thứ tự hiển thị của hình ảnh cho ProductUnit này
     */
    @Column(name = "display_order")
    private Integer displayOrder = 0;

    /**
     * Đánh dấu hình ảnh chính cho ProductUnit
     */
    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    /**
     * Trạng thái hoạt động
     */
    @Column(name = "is_active")
    private Boolean isActive = true;

    /**
     * Thời gian tạo mapping
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * ProductUnit mà mapping này thuộc về
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_unit_id", nullable = false)
    private ProductUnit productUnit;

    /**
     * ProductImage được chọn cho ProductUnit
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_image_id", nullable = false)
    private ProductImage productImage;

    /**
     * Constructor với các tham số chính
     */
    public ProductUnitImage(ProductUnit productUnit, ProductImage productImage, Integer displayOrder,
            Boolean isPrimary) {
        this.productUnit = productUnit;
        this.productImage = productImage;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
        this.isPrimary = isPrimary != null ? isPrimary : false;
        this.isActive = true;
    }
}
