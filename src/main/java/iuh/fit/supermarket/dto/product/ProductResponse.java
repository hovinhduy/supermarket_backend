package iuh.fit.supermarket.dto.product;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho phản hồi thông tin sản phẩm
 */
@Data
public class ProductResponse {

    /**
     * ID sản phẩm
     */
    private Long id;

    /**
     * Mã sản phẩm
     */
    private String code;

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
     * Loại sản phẩm: 1-Đơn giản, 2-Có biến thể
     */
    private Integer productType;

    /**
     * Có biến thể hay không
     */
    private Boolean hasVariants;

    /**
     * Số lượng biến thể
     */
    private Integer variantCount;

    /**
     * Giá gốc
     */
    private BigDecimal basePrice;

    /**
     * Giá vốn
     */
    private BigDecimal cost;

    /**
     * Giá nhập gần nhất
     */
    private BigDecimal latestPurchasePrice;

    /**
     * Đơn vị cơ bản
     */
    private String unit;

    /**
     * Giá trị quy đổi
     */
    private Integer conversionValue;

    /**
     * Số lượng tồn kho
     */
    private BigDecimal onHand;

    /**
     * Số lượng đang đặt hàng
     */
    private BigDecimal onOrder;

    /**
     * Số lượng đã đặt trước
     */
    private BigDecimal reserved;

    /**
     * Số lượng tối thiểu
     */
    private BigDecimal minQuantity;

    /**
     * Số lượng tối đa
     */
    private BigDecimal maxQuantity;

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
     * Thời gian tạo
     */
    private LocalDateTime createdDate;

    /**
     * Thời gian cập nhật
     */
    private LocalDateTime modifiedDate;

    /**
     * Thông tin danh mục
     */
    private CategoryDto category;

    /**
     * Danh sách đơn vị sản phẩm
     */
    private List<ProductUnitDto> productUnits;

    /**
     * Danh sách thuộc tính
     */
    private List<ProductAttributeDto> attributes;

    /**
     * DTO cho thông tin danh mục
     */
    @Data
    public static class CategoryDto {
        private Long id;
        private String name;
    }

    /**
     * DTO cho đơn vị sản phẩm
     */
    @Data
    public static class ProductUnitDto {
        private Long id;
        private String code;
        private String unit;
        private BigDecimal basePrice;
        private Integer conversionValue;
        private Boolean allowsSale;
        private String barcode;
    }

    /**
     * DTO cho thuộc tính sản phẩm
     */
    @Data
    public static class ProductAttributeDto {
        private Long id;
        private String attributeName;
        private String value;
    }

    /**
     * DTO cho vị trí kho
     */
    @Data
    public static class ProductShelfDto {
        private Long id;
        private String shelfName;
    }
}
