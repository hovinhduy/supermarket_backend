package iuh.fit.supermarket.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho phản hồi thông tin sản phẩm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin sản phẩm")
public class ProductResponse {

    /**
     * ID sản phẩm
     */
    @Schema(description = "ID sản phẩm", example = "1")
    private Long id;

    /**
     * Tên sản phẩm
     */
    @Schema(description = "Tên sản phẩm", example = "Smartphone Samsung Galaxy S24")
    private String name;

    /**
     * Mô tả sản phẩm
     */
    @Schema(description = "Mô tả sản phẩm", example = "Điện thoại thông minh cao cấp với camera chất lượng cao")
    private String description;

    /**
     * Trạng thái hoạt động
     */
    @Schema(description = "Trạng thái hoạt động", example = "true")
    private Boolean isActive;

    /**
     * Trạng thái xóa mềm
     */
    @Schema(description = "Trạng thái xóa mềm", example = "false")
    private Boolean isDeleted;

    /**
     * Có tích điểm thưởng không
     */
    @Schema(description = "Có tích điểm thưởng không", example = "true")
    private Boolean isRewardPoint;

    /**
     * Thời gian tạo
     */
    @Schema(description = "Thời gian tạo", example = "2024-01-01T10:00:00")
    private LocalDateTime createdDate;

    /**
     * Thời gian cập nhật
     */
    @Schema(description = "Thời gian cập nhật", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;

    /**
     * Thông tin thương hiệu
     */
    @Schema(description = "Thông tin thương hiệu")
    private BrandInfo brand;

    /**
     * Thông tin danh mục
     */
    @Schema(description = "Thông tin danh mục")
    private CategoryInfo category;

    /**
     * Số lượng đơn vị đóng gói
     */
    @Schema(description = "Số lượng đơn vị đóng gói", example = "3")
    private Integer unitCount;

    /**
     * Số lượng hình ảnh
     */
    @Schema(description = "Số lượng hình ảnh", example = "5")
    private Integer imageCount;

    /**
     * Danh sách đơn vị sản phẩm
     */
    @Schema(description = "Danh sách đơn vị sản phẩm")
    private List<ProductUnitInfo> productUnits;

    /**
     * Thông tin cơ bản của thương hiệu
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thông tin cơ bản thương hiệu")
    public static class BrandInfo {

        /**
         * ID thương hiệu
         */
        @Schema(description = "ID thương hiệu", example = "1")
        private Integer brandId;

        /**
         * Tên thương hiệu
         */
        @Schema(description = "Tên thương hiệu", example = "Samsung")
        private String name;

        /**
         * Mã thương hiệu
         */
        @Schema(description = "Mã thương hiệu", example = "BR0001")
        private String brandCode;

        /**
         * URL logo thương hiệu
         */
        @Schema(description = "URL logo thương hiệu", example = "https://example.com/logo.png")
        private String logoUrl;
    }

    /**
     * Thông tin cơ bản của danh mục
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thông tin cơ bản danh mục")
    public static class CategoryInfo {

        /**
         * ID danh mục
         */
        @Schema(description = "ID danh mục", example = "1")
        private Integer categoryId;

        /**
         * Tên danh mục
         */
        @Schema(description = "Tên danh mục", example = "Điện thoại")
        private String name;

        /**
         * Mô tả danh mục
         */
        @Schema(description = "Mô tả danh mục", example = "Các thiết bị điện thoại thông minh")
        private String description;
    }

    /**
     * Thông tin đơn vị sản phẩm
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thông tin đơn vị sản phẩm")
    public static class ProductUnitInfo {

        /**
         * ID đơn vị sản phẩm
         */
        @Schema(description = "ID đơn vị sản phẩm", example = "1")
        private Long id;

        /**
         * Mã đơn vị sản phẩm
         */
        @Schema(description = "Mã đơn vị sản phẩm", example = "PU1U1T12345")
        private String code;

        /**
         * Mã vạch
         */
        @Schema(description = "Mã vạch", example = "1234567890123")
        private String barcode;

        /**
         * Tỷ lệ quy đổi
         */
        @Schema(description = "Tỷ lệ quy đổi so với đơn vị cơ bản", example = "1")
        private Integer conversionValue;

        /**
         * Có phải đơn vị cơ bản không
         */
        @Schema(description = "Có phải đơn vị cơ bản không", example = "true")
        private Boolean isBaseUnit;

        /**
         * Trạng thái hoạt động
         */
        @Schema(description = "Trạng thái hoạt động", example = "true")
        private Boolean isActive;

        /**
         * Tên đơn vị tính
         */
        @Schema(description = "Tên đơn vị tính", example = "Kilogram")
        private String unitName;
    }
}
