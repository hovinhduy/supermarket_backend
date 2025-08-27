package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho tồn kho sản phẩm trong hệ thống
 */
@Entity
@Table(name = "inventory", uniqueConstraints = @UniqueConstraint(columnNames = { "variant_id", "warehouse_id" }))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    /**
     * ID duy nhất của bản ghi tồn kho
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Integer inventoryId;

    /**
     * ID kho hàng
     */
    @Column(name = "warehouse_id")
    private Integer warehouseId = 1;

    /**
     * Số lượng tồn kho hiện tại
     */
    @Column(name = "quantity_on_hand", nullable = false)
    private Integer quantityOnHand = 0;

    /**
     * Số lượng đã được đặt trước
     */
    @Column(name = "quantity_reserved", nullable = false)
    private Integer quantityReserved = 0;

    /**
     * Điểm đặt hàng lại
     */
    @Column(name = "reorder_point")
    private Integer reorderPoint = 0;

    /**
     * Thời gian cập nhật
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Biến thể sản phẩm
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    /**
     * Tính số lượng có thể bán
     * 
     * @return số lượng có thể bán (tồn kho - đã đặt trước)
     */
    public Integer getAvailableQuantity() {
        return quantityOnHand - quantityReserved;
    }

    /**
     * Kiểm tra có cần đặt hàng lại không
     * 
     * @return true nếu cần đặt hàng lại
     */
    public Boolean needsReorder() {
        return quantityOnHand <= reorderPoint;
    }
}
