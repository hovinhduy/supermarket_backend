package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity đại diện cho thương hiệu sản phẩm trong hệ thống
 */
@Entity
@Table(name = "brands")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Brand {

    /**
     * ID duy nhất của thương hiệu
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_id")
    private Integer brandId;

    /**
     * Tên thương hiệu (duy nhất)
     */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * Mã thương hiệu (duy nhất)
     */
    @Column(name = "brand_code", length = 50, unique = true)
    private String brandCode;

    /**
     * URL logo thương hiệu
     */
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    /**
     * Mô tả thương hiệu
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Trạng thái hoạt động
     */
    @Column(name = "is_active")
    private Boolean isActive = true;

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
     * Danh sách sản phẩm của thương hiệu này
     */
    @OneToMany(mappedBy = "brand", fetch = FetchType.LAZY)
    private List<Product> products;
}
