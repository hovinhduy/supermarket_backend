package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho tồn kho sản phẩm trong hệ thống
 * CHỈ LƯU TỒN KHO CHO BIẾN THỂ CÓ ĐỚN VỊ CƠ BẢN (isBaseUnit = true)
 * Số lượng của các biến thể khác được tính toán dựa trên conversionValue
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
     * Số lượng tồn kho hiện tại (theo đơn vị cơ bản)
     * Chỉ lưu cho biến thể có unit.isBaseUnit = true
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
     * Biến thể sản phẩm
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    /**
     * Kho hàng
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;
}
