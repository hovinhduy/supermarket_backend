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
     * Số lượng có thể bán
     */
    private Integer availableQuantity;

    /**
     * Thời gian cập nhật
     */
    private LocalDateTime updatedAt;
}
