package iuh.fit.supermarket.dto.product;

import lombok.Data;

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

    // Đã loại bỏ thông tin giá - sẽ được quản lý riêng trong hệ thống giá

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
     * DTO cho đơn vị biến thể (chỉ thông tin đơn vị, không có giá)
     */
    @Data
    public static class VariantUnitDto {
        private String unit;
        private Integer conversionValue;
        private String barcode;
    }
}
