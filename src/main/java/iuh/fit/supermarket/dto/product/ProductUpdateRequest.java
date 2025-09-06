package iuh.fit.supermarket.dto.product;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO cho yêu cầu cập nhật sản phẩm
 */
@Data
public class ProductUpdateRequest {

    /**
     * Tên sản phẩm
     */
    private String name;

    /**
     * Tên đầy đủ của sản phẩm
     */
    private String fullName;

    /**
     * Mô tả sản phẩm
     */
    private String description;

    /**
     * ID danh mục sản phẩm
     */
    private Long categoryId;

    /**
     * Giá gốc
     */
    private BigDecimal basePrice;

    /**
     * Giá vốn
     */
    private BigDecimal cost;

    /**
     * Đơn vị cơ bản
     */
    private String unit;

    /**
     * Mã vạch
     */
    private String barcode;

    /**
     * Tên thương hiệu
     */
    private String tradeMarkName;

    /**
     * Cho phép bán hay không
     */
    private Boolean allowsSale;

    /**
     * Trạng thái hoạt động
     */
    private Boolean isActive;

    /**
     * Số lượng tối thiểu
     */
    private BigDecimal minQuantity;

    /**
     * Số lượng tối đa
     */
    private BigDecimal maxQuantity;

    /**
     * Danh sách thuộc tính cập nhật
     */
    private List<ProductAttributeUpdateDto> attributes;

    /**
     * DTO cho cập nhật thuộc tính sản phẩm
     */
    @Data
    public static class ProductAttributeUpdateDto {
        private Long attributeId;
        private String value;
    }
}
