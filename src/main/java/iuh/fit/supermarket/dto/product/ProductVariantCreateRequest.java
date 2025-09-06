package iuh.fit.supermarket.dto.product;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho yêu cầu tạo biến thể sản phẩm
 */
@Data
public class ProductVariantCreateRequest {

    /**
     * Danh sách thuộc tính phân biệt biến thể
     */
    private List<VariantAttributeDto> attributes;

    /**
     * Thông tin giá cả
     */
    private PricingDto pricing;

    /**
     * Danh sách đơn vị cho biến thể
     */
    private List<VariantUnitDto> units;

    /**
     * DTO cho thuộc tính biến thể
     */
    @Data
    public static class VariantAttributeDto {
        private Long attributeId;
        private String value;
    }

    /**
     * DTO cho thông tin giá
     */
    @Data
    public static class PricingDto {
        private BigDecimal basePrice;
        private BigDecimal cost;
    }

    /**
     * DTO cho đơn vị biến thể
     */
    @Data
    public static class VariantUnitDto {
        private String unit;
        private BigDecimal basePrice;
        private Integer conversionValue;
        private String barcode;
    }
}
