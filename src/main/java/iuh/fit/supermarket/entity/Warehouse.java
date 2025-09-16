package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Entity đại diện cho kho hàng trong hệ thống
 * Quản lý nhiều kho/cửa hàng
 * Theo database schema mới: warehouse_id, name, address
 */
@Entity
@Table(name = "Warehouses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse {

    /**
     * ID duy nhất của kho hàng
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "warehouse_id")
    private Integer warehouseId;

    /**
     * Tên kho hàng (bắt buộc)
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * Địa chỉ kho hàng
     */
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    /**
     * Danh sách tồn kho tại kho này
     */
    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Inventory> inventories;

    /**
     * Danh sách giao dịch kho tại kho này
     */
    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InventoryTransaction> inventoryTransactions;
}
