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
 * Entity đại diện cho danh mục sản phẩm trong hệ thống
 */
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    /**
     * ID duy nhất của danh mục
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId;

    /**
     * Tên danh mục (duy nhất)
     */
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    /**
     * Mô tả danh mục
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

    // /**
    // * Cấp độ của danh mục trong cây phân cấp (0 = gốc, 1 = cấp 1, ...)
    // */
    // @Column(name = "level", nullable = false)
    // private Integer level = 0;

    /**
     * Danh mục cha (tự tham chiếu)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    /**
     * Danh sách danh mục con
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Category> children;

    /**
     * Danh sách sản phẩm thuộc danh mục này
     */
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Product> products;
}
