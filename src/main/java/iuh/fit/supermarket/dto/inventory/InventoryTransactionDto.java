package iuh.fit.supermarket.dto.inventory;

import iuh.fit.supermarket.entity.InventoryTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO cho thông tin giao dịch tồn kho
 * Sử dụng để trả về dữ liệu API mà không gây LazyInitializationException
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransactionDto {

    /**
     * ID duy nhất của giao dịch kho
     */
    private Long transactionId;

    /**
     * Số lượng thay đổi
     * Số dương cho nhập hàng/trả hàng, số âm cho bán hàng/xuất hủy
     */
    private Integer quantityChange;

    /**
     * Số lượng tồn kho sau khi thực hiện giao dịch
     */
    private Integer newQuantity;

    /**
     * Giá vốn đơn vị tại thời điểm giao dịch
     */
    private BigDecimal unitCostPrice;

    /**
     * Loại giao dịch
     */
    private InventoryTransaction.TransactionType transactionType;

    /**
     * Mã đơn hàng, mã phiếu nhập, mã phiếu kiểm kê...
     */
    private String referenceId;

    /**
     * Ghi chú
     */
    private String notes;

    /**
     * Thời gian giao dịch
     */
    private LocalDateTime transactionDate;

    // Thông tin biến thể sản phẩm (eager loaded)
    /**
     * ID biến thể sản phẩm
     */
    private Long variantId;

    /**
     * Mã biến thể sản phẩm
     */
    private String variantCode;

    /**
     * Tên biến thể sản phẩm
     */
    private String variantName;

    // Thông tin kho hàng (eager loaded)
    /**
     * ID kho hàng
     */
    private Integer warehouseId;

    /**
     * Tên kho hàng
     */
    private String warehouseName;
}
