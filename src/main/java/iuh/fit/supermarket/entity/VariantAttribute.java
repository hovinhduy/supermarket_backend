package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho liên kết giữa biến thể sản phẩm và giá trị thuộc tính
 * Bảng trung gian để định nghĩa các thuộc tính của một biến thể cụ thể
 */
@Entity
@Table(name = "variant_attributes", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "variant_id", "attribute_value_id" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariantAttribute {

    /**
     * ID duy nhất của liên kết thuộc tính biến thể
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * Thời gian tạo
     */
    @CreationTimestamp
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    /**
     * Biến thể sản phẩm
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    /**
     * Giá trị thuộc tính định nghĩa biến thể này
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_value_id", nullable = false)
    private AttributeValue attributeValue;
}
