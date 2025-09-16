package iuh.fit.supermarket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho lịch sử giao dịch kho trong hệ thống
 * "Sổ cái" ghi lại TOÀN BỘ thay đổi về số lượng tồn kho
 * Đây là nguồn dữ liệu gốc, không bao giờ xóa
 */
@Entity
@Table(name = "InventoryTransactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransaction {

    /**
     * ID duy nhất của giao dịch kho
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    /**
     * Số lượng thay đổi
     * Số dương cho nhập hàng/trả hàng, số âm cho bán hàng/xuất hủy
     */
    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange;

    /**
     * Số lượng tồn kho sau khi thực hiện giao dịch
     */
    @Column(name = "new_quantity", nullable = false)
    private Integer newQuantity;

    /**
     * Giá vốn đơn vị tại thời điểm giao dịch
     */
    @Column(name = "unit_cost_price", precision = 15, scale = 2)
    private BigDecimal unitCostPrice;

    /**
     * Loại giao dịch
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    /**
     * Mã đơn hàng, mã phiếu nhập, mã phiếu kiểm kê...
     */
    @Column(name = "reference_id", length = 255)
    private String referenceId;

    /**
     * Ghi chú
     */
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    /**
     * Thời gian giao dịch
     */
    @CreationTimestamp
    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

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

    /**
     * Enum cho loại giao dịch
     */
    public enum TransactionType {
        STOCK_IN, // Nhập hàng
        SALE, // Bán hàng
        RETURN, // Trả hàng
        ADJUSTMENT, // Điều chỉnh
        TRANSFER_OUT, // Chuyển kho (xuất)
        TRANSFER_IN // Chuyển kho (nhập)
    }
}
