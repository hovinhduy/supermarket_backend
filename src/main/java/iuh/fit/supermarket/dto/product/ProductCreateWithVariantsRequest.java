package iuh.fit.supermarket.dto.product;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho yêu cầu tạo sản phẩm mới với nhiều biến thể cùng lúc
 */
@Data
public class ProductCreateWithVariantsRequest {

    /**
     * Tên sản phẩm
     */
    private String name;

    /**
     * ID danh mục sản phẩm
     */
    private Long categoryId;

    /**
     * ID thương hiệu sản phẩm
     */
    private Long brandId;

    /**
     * Mô tả sản phẩm
     */
    private String description;

    /**
     * Cho phép bán hay không
     */
    private Boolean allowsSale;

    /**
     * Danh sách biến thể sản phẩm
     */
    private List<VariantDto> variants;

    /**
     * DTO cho biến thể sản phẩm
     */
    @Data
    public static class VariantDto {

        /**
         * Danh sách thuộc tính phân biệt biến thể
         */
        private List<VariantAttributeDto> attributes;

        /**
         * Danh sách đơn vị cho biến thể
         */
        private List<VariantUnitDto> units;
    }

    /**
     * DTO cho thuộc tính biến thể
     */
    @Data
    public static class VariantAttributeDto {
        /**
         * ID thuộc tính
         */
        private Long attributeId;

        /**
         * Giá trị thuộc tính
         */
        private String value;
    }

    /**
     * DTO cho đơn vị biến thể
     */
    @Data
    public static class VariantUnitDto {
        /**
         * Tên đơn vị
         */
        private String unit;

        /**
         * Giá trị chuyển đổi
         */
        private Integer conversionValue;

        /**
         * Mã barcode
         */
        private String barcode;
        /**
         * Mã SKU của biến thể
         */
        private String variantCode;

        /**
         * Đánh dấu đơn vị này là đơn vị cơ bản của sản phẩm
         */
        private Boolean isBaseUnit;
    }
}
