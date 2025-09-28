package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho kho hàng và tồn kho sản phẩm trong hệ thống
 * Số lượng của các đơn vị khác được tính toán dựa trên conversionValue
 * Hệ thống chỉ có 1 kho duy nhất
 */
@Entity
@Table(name = "warehouses", uniqueConstraints = @UniqueConstraint(columnNames = { "product_unit_id" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse {

    /**
     * ID duy nhất của bản ghi kho hàng
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "warehouse_id")
    private Integer warehouseId;

    /**
     * Số lượng tồn kho hiện tại
     */
    @Column(name = "quantity_on_hand", nullable = false)
    private Integer quantityOnHand = 0;

    /**
     * Thời gian cập nhật
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Đơn vị sản phẩm
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_unit_id", nullable = false)
    private ProductUnit productUnit;
}
