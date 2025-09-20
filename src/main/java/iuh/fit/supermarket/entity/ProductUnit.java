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
 * Entity đại diện cho đơn vị đóng gói sản phẩm trong hệ thống
 * Chỉ định nghĩa đơn vị và tỷ lệ quy đổi, không chứa thông tin giá
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
     * Tỷ lệ quy đổi so với đơn vị cơ bản
     * Ví dụ: lon=1, lốc=6, thùng=24
     */
    @Column(name = "conversion_value", nullable = false)
    private Integer conversionValue = 1;

    /**
     * Thứ tự sắp xếp hiển thị
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * Đánh dấu đơn vị cơ bản của sản phẩm
     * Chỉ có một đơn vị cơ bản cho mỗi sản phẩm
     */
    @Column(name = "is_base_unit")
    private Boolean isBaseUnit = false;

    /**
     * Trạng thái hoạt động
     */
    @Column(name = "is_active")
    private Boolean isActive = true;

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
}
