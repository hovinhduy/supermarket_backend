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
     * Cho phép bán hay không
     */
    private Boolean allowsSale;

    /**
     * Trạng thái hoạt động
     */
    private Boolean isActive;
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
        private Boolean isBaseUnit;
    }
}
