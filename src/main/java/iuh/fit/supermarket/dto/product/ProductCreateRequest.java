package iuh.fit.supermarket.dto.product;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho yêu cầu tạo sản phẩm mới
 */
@Data
public class ProductCreateRequest {

    /**
     * Tên sản phẩm
     */
    private String name;

    /**
     * ID danh mục sản phẩm
     */
    private Long categoryId;

    /**
     * Loại sản phẩm: 1-Đơn giản, 2-Có biến thể
     */
    private Integer productType;

    /**
     * Thông tin đơn vị cơ bản
     */
    private BaseUnitDto baseUnit;

    /**
     * Danh sách đơn vị bổ sung
     */
    private List<AdditionalUnitDto> additionalUnits;

    /**
     * Danh sách thuộc tính
     */
    private List<ProductAttributeDto> attributes;

    /**
     * Thông tin tồn kho
     */
    private InventoryDto inventory;

    /**
     * Cho phép bán hay không
     */
    private Boolean allowsSale;

    /**
     * Mô tả sản phẩm
     */
    private String description;

    /**
     * DTO cho đơn vị cơ bản
     */
    @Data
    public static class BaseUnitDto {
        private String unit;
        private BigDecimal basePrice;
        private BigDecimal cost;
        private String barcode;
    }

    /**
     * DTO cho đơn vị bổ sung
     */
    @Data
    public static class AdditionalUnitDto {
        private String unit;
        private BigDecimal basePrice;
        private Integer conversionValue;
        private String barcode;
    }

    /**
     * DTO cho thuộc tính sản phẩm
     */
    @Data
    public static class ProductAttributeDto {
        private Long attributeId;
        private String value;
    }

    /**
     * DTO cho thông tin tồn kho
     */
    @Data
    public static class InventoryDto {
        private BigDecimal minQuantity;
        private BigDecimal maxQuantity;
        private BigDecimal onHand;
    }
}
