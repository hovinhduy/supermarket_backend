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
     * Số lượng đã được đặt trước (theo đơn vị cơ bản)
     */
    @Column(name = "quantity_reserved", nullable = false)
    private Integer quantityReserved = 0;

    /**
     * Điểm đặt hàng lại (theo đơn vị cơ bản)
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
     * Biến thể sản phẩm (CHỈ CHẤP NHẬN biến thể có unit.isBaseUnit = true)
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

    /**
     * Tính số lượng có thể bán (theo đơn vị cơ bản)
     *
     * @return số lượng có thể bán (tồn kho - đã đặt trước)
     */
    public Integer getAvailableQuantity() {
        return quantityOnHand - quantityReserved;
    }

    /**
     * Kiểm tra có cần đặt hàng lại không (theo đơn vị cơ bản)
     *
     * @return true nếu cần đặt hàng lại
     */
    public Boolean needsReorder() {
        return quantityOnHand <= reorderPoint;
    }

    /**
     * Tính số lượng có thể bán cho biến thể cụ thể dựa trên conversionValue
     *
     * @param targetConversionValue tỷ lệ quy đổi của biến thể cần tính
     * @return số lượng có thể bán theo đơn vị của biến thể đó
     */
    public Integer getAvailableQuantityForUnit(Integer targetConversionValue) {
        if (targetConversionValue == null || targetConversionValue <= 0) {
            return 0;
        }
        return getAvailableQuantity() / targetConversionValue;
    }

    /**
     * Tính tổng số lượng tồn kho cho biến thể cụ thể dựa trên conversionValue
     *
     * @param targetConversionValue tỷ lệ quy đổi của biến thể cần tính
     * @return số lượng tồn kho theo đơn vị của biến thể đó
     */
    public Integer getQuantityOnHandForUnit(Integer targetConversionValue) {
        if (targetConversionValue == null || targetConversionValue <= 0) {
            return 0;
        }
        return quantityOnHand / targetConversionValue;
    }
}
