package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity đại diện cho hình ảnh sản phẩm trong hệ thống
 */
@Entity
@Table(name = "product_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {

    /**
     * ID duy nhất của hình ảnh
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Integer imageId;

    /**
     * URL của hình ảnh
     */
    @Column(name = "image_url", length = 500, nullable = false)
    private String imageUrl;

    /**
     * Văn bản thay thế cho hình ảnh
     */
    @Column(name = "image_alt")
    private String imageAlt;

    /**
     * Thứ tự sắp xếp
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * Thời gian tạo
     */
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * Sản phẩm mà hình ảnh này thuộc về
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Danh sách mapping với các ProductUnit sử dụng hình ảnh này
     */
    @OneToMany(mappedBy = "productImage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductUnitImage> productUnitImages;
}
