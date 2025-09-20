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
 * Entity đại diện cho giá trị cụ thể của thuộc tính (ví dụ: Đỏ, Xanh, L, XL...)
 */
@Entity
@Table(name = "attribute_values")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttributeValue {

    /**
     * ID duy nhất của giá trị thuộc tính
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Giá trị cụ thể của thuộc tính
     */
    @Column(name = "value", length = 255, nullable = false)
    private String value;

    /**
     * Mô tả về giá trị thuộc tính (tùy chọn)
     */
    @Column(name = "description", length = 500)
    private String description;

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
     * Thuộc tính mà giá trị này thuộc về
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private Attribute attribute;

    /**
     * Danh sách các biến thể sử dụng giá trị thuộc tính này
     */
    @OneToMany(mappedBy = "attributeValue", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VariantAttribute> variantAttributes;
}
