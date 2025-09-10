package iuh.fit.supermarket.dto.product;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho thông tin biến thể sản phẩm
 */
@Data
public class ProductVariantDto {

    /**
     * ID của biến thể
     */
    private Long variantId;

    /**
     * Tên biến thể
     */
    private String variantName;

    /**
     * Mã biến thể (SKU)
     */
    private String variantCode;

    /**
     * Mã vạch
     */
    private String barcode;

    /**
     * Giá vốn
     */
    private BigDecimal costPrice;

    /**
     * Giá bán cơ bản
     */
    private BigDecimal basePrice;

    /**
     * Số lượng tồn kho
     */
    private BigDecimal quantityOnHand;

    /**
     * Số lượng đã đặt trước
     */
    private BigDecimal quantityReserved;

    /**
     * Số lượng có thể bán
     */
    private BigDecimal availableQuantity;

    /**
     * Số lượng tối thiểu
     */
    private BigDecimal minQuantity;

    /**
     * Cho phép bán hay không
     */
    private Boolean allowsSale;

    /**
     * Trạng thái hoạt động
     */
    private Boolean isActive;

    /**
     * Cần đặt hàng lại hay không
     */
    private Boolean needsReorder;

    /**
     * Thời gian tạo
     */
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật
     */
    private LocalDateTime updatedAt;

    /**
     * Thông tin đơn vị
     */
    private ProductUnitDto unit;

    /**
     * Danh sách thuộc tính biến thể
     */
    private List<VariantAttributeDto> attributes;

    /**
     * Danh sách hình ảnh biến thể
     */
    private List<ProductImageDto> images;

    /**
     * DTO cho đơn vị sản phẩm (đơn giản hóa)
     */
    @Data
    public static class ProductUnitDto {
        private Long id;
        private String code;
        private String unit;
        private Integer conversionValue;
    }
}
