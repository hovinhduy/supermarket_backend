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
 * Entity đại diện cho biến thể sản phẩm (SKU) trong hệ thống
 * Chỉ chứa thông tin cơ bản của biến thể.
 */
@Entity
@Table(name = "product_variants", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "variant_code" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {

    /**
     * ID duy nhất của biến thể
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variant_id")
    private Long variantId;

    /**
     * Tên đầy đủ của SKU
     * Ví dụ: "Áo Thun Polo - Đỏ - L - Cái"
     */
    @Column(name = "variant_name", nullable = false)
    private String variantName;

    /**
     * Mã biến thể (duy nhất) - Mã hàng chính cho bán hàng
     */
    @Column(name = "variant_code", length = 100, nullable = false, unique = true)
    private String variantCode;

    /**
     * Mã vạch cho quét
     */
    @Column(name = "barcode", length = 100)
    private String barcode;

    /**
     * Cho phép bán hay không
     */
    @Column(name = "allows_sale")
    private Boolean allowsSale = true;

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
     * Sản phẩm mà biến thể này thuộc về
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /**
     * Đơn vị đóng gói của biến thể này
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private ProductUnit unit;

    /**
     * Danh sách các thuộc tính định nghĩa biến thể này (màu sắc, kích thước...)
     */
    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<VariantAttribute> variantAttributes;

    /**
     * Danh sách hình ảnh riêng cho biến thể này
     */
    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductImage> images;

    /**
     * Lịch sử giá của biến thể
     */
    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PriceHistory> priceHistories;

    /**
     * Thông tin tồn kho
     */
    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Inventory> inventories;

    /**
     * Lịch sử xuất nhập kho
     */
    @OneToMany(mappedBy = "variant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StockMovement> stockMovements;

    /**
     * Chi tiết nhập hàng
     */
    @OneToMany(mappedBy = "variant", fetch = FetchType.LAZY)
    private List<ImportDetail> importDetails;

    /**
     * Các mục trong giỏ hàng
     */
    @OneToMany(mappedBy = "variant", fetch = FetchType.LAZY)
    private List<CartItem> cartItems;

    /**
     * Chi tiết đơn hàng
     */
    @OneToMany(mappedBy = "variant", fetch = FetchType.LAZY)
    private List<OrderDetail> orderDetails;
}
