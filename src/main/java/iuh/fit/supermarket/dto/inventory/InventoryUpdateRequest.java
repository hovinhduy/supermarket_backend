package iuh.fit.supermarket.dto.inventory;

import io.swagger.v3.oas.annotations.media.Schema;
import iuh.fit.supermarket.entity.InventoryTransaction;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO cho yêu cầu cập nhật tồn kho
 */
@Data
public class InventoryUpdateRequest {

    /**
     * ID biến thể sản phẩm
     */
    @Schema(description = "ID biến thể sản phẩm", example = "1", required = true)
    private Long variantId;

    /**
     * ID kho hàng (mặc định là kho chính nếu không chỉ định)
     */
    @Schema(description = "ID kho hàng", example = "1")
    private Integer warehouseId;

    /**
     * Thay đổi số lượng (dương = nhập, âm = xuất)
     */
    @Schema(description = "Thay đổi số lượng (dương = nhập, âm = xuất)", example = "10", required = true)
    private Integer quantityChange;

    /**
     * Loại giao dịch
     */
    @Schema(description = "Loại giao dịch", example = "STOCK_IN", required = true)
    private InventoryTransaction.TransactionType transactionType;

    /**
     * Giá vốn đơn vị tại thời điểm giao dịch
     */
    @Schema(description = "Giá vốn đơn vị", example = "50000")
    private BigDecimal unitCostPrice;

    /**
     * Mã tham chiếu (mã đơn hàng, phiếu nhập...)
     */
    @Schema(description = "Mã tham chiếu", example = "PO-2024-001")
    private String referenceId;

    /**
     * Ghi chú
     */
    @Schema(description = "Ghi chú về giao dịch", example = "Nhập hàng từ nhà cung cấp ABC")
    private String notes;

    /**
     * Constructor với các tham số bắt buộc
     */
    public InventoryUpdateRequest(Long variantId, Integer quantityChange, 
                                InventoryTransaction.TransactionType transactionType) {
        this.variantId = variantId;
        this.quantityChange = quantityChange;
        this.transactionType = transactionType;
    }

    /**
     * Constructor mặc định
     */
    public InventoryUpdateRequest() {
    }
}
