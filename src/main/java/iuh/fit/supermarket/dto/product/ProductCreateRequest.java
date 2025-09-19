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
        private String barcode;
        /**
         * Mã SKU của biến thể (tùy chọn, sẽ tự động tạo nếu không cung cấp)
         */
        private String variantCode;
    }

    /**
     * DTO cho đơn vị bổ sung
     */
    @Data
    public static class AdditionalUnitDto {
        private String unit;
        private Integer conversionValue;
        private String barcode;
        /**
         * Mã SKU của biến thể (tùy chọn, sẽ tự động tạo nếu không cung cấp)
         */
        private String variantCode;
    }

    /**
     * DTO cho thuộc tính sản phẩm
     */
    @Data
    public static class ProductAttributeDto {
        private Long attributeId;
        private String value;
    }

}
