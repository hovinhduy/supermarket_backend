package iuh.fit.supermarket.dto.inventory;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO cho thông tin tồn kho sản phẩm
 */
@Data
public class InventoryDto {

    /**
     * ID của bản ghi tồn kho
     */
    private Integer inventoryId;

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

    /**
     * ID kho hàng
     */
    private Integer warehouseId;

    /**
     * Tên kho hàng
     */
    private String warehouseName;

    /**
     * Số lượng tồn kho hiện tại
     */
    private Integer quantityOnHand;

    /**
     * Số lượng đã được đặt trước
     */
    private Integer quantityReserved;

    /**
     * Số lượng có thể bán
     */
    private Integer availableQuantity;

    /**
     * Điểm đặt hàng lại
     */
    private Integer reorderPoint;

    /**
     * Cần đặt hàng lại hay không
     */
    private Boolean needsReorder;

    /**
     * Thời gian cập nhật
     */
    private LocalDateTime updatedAt;
}
