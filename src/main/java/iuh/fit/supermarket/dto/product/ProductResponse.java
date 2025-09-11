package iuh.fit.supermarket.dto.product;

import lombok.Data;

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
     * Thương hiệu sản phẩm
     */
    private BrandDto brand;

    /**
     * Danh sách biến thể sản phẩm
     */
    private List<ProductVariantDto> variants;

    /**
     * DTO cho thông tin danh mục
     */
    @Data
    public static class CategoryDto {
        private Long id;
        private String name;
    }

    /**
     * Dto Thương hiệu sản phẩm
     */
    @Data
    public static class BrandDto {
        private Long id;
        private String name;
    }
}
