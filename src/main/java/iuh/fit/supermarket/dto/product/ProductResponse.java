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
     * Tên sản phẩm
     */
    private String name;

    /**
     * Mô tả sản phẩm
     */
    private String description;

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
    private LocalDateTime updatedAt;

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
