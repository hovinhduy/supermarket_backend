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
 * Định nghĩa mối quan hệ giữa sản phẩm và đơn vị tính, bao gồm tỷ lệ quy đổi và thông tin bổ sung
 */
@Entity
@Table(name = "product_units", uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_unit_product_unit", columnNames = {"product_id", "unit_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUnit {

    /**
     * ID duy nhất của đơn vị sản phẩm
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Mã vạch của đơn vị sản phẩm
     */
    @Column(name = "barcode", length = 100)
    private String barcode;

    /**
     * Tỷ lệ quy đổi so với đơn vị cơ bản
     * Ví dụ: nếu đơn vị cơ bản là "cái" và đơn vị này là "thùng",
     * conversionValue = 24 có nghĩa là 1 thùng = 24 cái
     */
    @Column(name = "conversion_value", nullable = false)
    private Integer conversionValue = 1;


    /**
     * Đánh dấu đơn vị cơ bản của sản phẩm
     * Chỉ có một đơn vị cơ bản cho mỗi sản phẩm (conversionValue = 1)
     */
    @Column(name = "is_base_unit")
    private Boolean isBaseUnit = false;

    /**
     * Trạng thái hoạt động
     */
    @Column(name = "is_active")
    private Boolean isActive = true;

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
     * Sản phẩm mà đơn vị này thuộc về
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Đơn vị tính được sử dụng
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private Unit unit;

    /**
     * Danh sách mapping với các hình ảnh được chọn cho đơn vị này
     */
    @OneToMany(mappedBy = "productUnit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductUnitImage> productUnitImages;
}
