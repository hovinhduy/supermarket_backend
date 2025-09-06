package iuh.fit.supermarket.entity;

import iuh.fit.supermarket.enums.ProductStatus;
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
    @Column(name = "id")
    private Long id;

    /**
     * Mã sản phẩm (duy nhất)
     */
    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;

    /**
     * Tên sản phẩm
     */
    @Column(name = "name", length = 255, nullable = false)
    private String name;

    /**
     * Tên đầy đủ của sản phẩm
     */
    @Column(name = "full_name", length = 500)
    private String fullName;

    /**
     * Mô tả sản phẩm
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Loại sản phẩm: 1-Đơn giản, 2-Có biến thể
     */
    @Column(name = "product_type", nullable = false)
    private Integer productType = 1;

    /**
     * Có biến thể hay không
     */
    @Column(name = "has_variants")
    private Boolean hasVariants = false;

    /**
     * Số lượng biến thể
     */
    @Column(name = "variant_count")
    private Integer variantCount = 0;

    /**
     * Giá gốc
     */
    @Column(name = "base_price", precision = 18, scale = 2)
    private BigDecimal basePrice;

    /**
     * Giá vốn
     */
    @Column(name = "cost", precision = 18, scale = 2)
    private BigDecimal cost;

    /**
     * Giá nhập gần nhất
     */
    @Column(name = "latest_purchase_price", precision = 18, scale = 2)
    private BigDecimal latestPurchasePrice;

    /**
     * Đơn vị cơ bản
     */
    @Column(name = "unit", length = 50)
    private String unit;

    /**
     * Giá trị quy đổi
     */
    @Column(name = "conversion_value")
    private Integer conversionValue = 1;

    /**
     * Số lượng tồn kho
     */
    @Column(name = "on_hand", precision = 18, scale = 2)
    private BigDecimal onHand = BigDecimal.ZERO;

    /**
     * Số lượng đang đặt hàng
     */
    @Column(name = "on_order", precision = 18, scale = 2)
    private BigDecimal onOrder = BigDecimal.ZERO;

    /**
     * Số lượng đã đặt trước
     */
    @Column(name = "reserved", precision = 18, scale = 2)
    private BigDecimal reserved = BigDecimal.ZERO;

    /**
     * Số lượng tối thiểu
     */
    @Column(name = "min_quantity", precision = 18, scale = 2)
    private BigDecimal minQuantity = BigDecimal.ZERO;

    /**
     * Số lượng tối đa
     */
    @Column(name = "max_quantity", precision = 18, scale = 2)
    private BigDecimal maxQuantity = new BigDecimal("999999999");

    /**
     * Mã vạch
     */
    @Column(name = "barcode", length = 100)
    private String barcode;

    /**
     * Tên thương hiệu
     */
    @Column(name = "trade_mark_name", length = 255)
    private String tradeMarkName;

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
     * Kiểm soát lô/serial
     */
    @Column(name = "is_lot_serial_control")
    private Boolean isLotSerialControl = false;

    /**
     * Có tích điểm thưởng không
     */
    @Column(name = "is_reward_point")
    private Boolean isRewardPoint = false;

    /**
     * Kiểm soát hạn sử dụng theo lô
     */
    @Column(name = "is_batch_expire_control")
    private Boolean isBatchExpireControl = false;

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
     * Thương hiệu của sản phẩm
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    /**
     * Danh mục của sản phẩm
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * Danh sách đơn vị đóng gói của sản phẩm
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductUnit> productUnits;

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

    /**
     * Danh sách thuộc tính của sản phẩm
     */
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProductAttribute> attributes;

}
