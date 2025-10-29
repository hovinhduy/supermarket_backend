package iuh.fit.supermarket.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho phản hồi danh sách sản phẩm với phân trang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Danh sách sản phẩm với thông tin phân trang")
public class ProductListResponse {

    /**
     * Danh sách sản phẩm
     */
    @Schema(description = "Danh sách sản phẩm")
    private List<ProductSummary> products;

    /**
     * Thông tin phân trang
     */
    @Schema(description = "Thông tin phân trang")
    private PageInfo pageInfo;

    /**
     * Thông tin tóm tắt sản phẩm cho danh sách
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thông tin tóm tắt sản phẩm")
    public static class ProductSummary {

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
         * Mô tả sản phẩm (rút gọn)
         */
        @Schema(description = "Mô tả sản phẩm", example = "Điện thoại thông minh cao cấp...")
        private String description;

        /**
         * Trạng thái hoạt động
         */
        @Schema(description = "Trạng thái hoạt động", example = "true")
        private Boolean isActive;

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
         * ID thương hiệu
         */
        @Schema(description = "ID thương hiệu", example = "1")
        private Integer brandId;

        /**
         * Tên thương hiệu
         */
        @Schema(description = "Tên thương hiệu", example = "Samsung")
        private String brandName;

        /**
         * ID danh mục
         */
        @Schema(description = "ID danh mục", example = "1")
        private Integer categoryId;

        /**
         * Tên danh mục
         */
        @Schema(description = "Tên danh mục", example = "Điện thoại")
        private String categoryName;

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
         * URL hình ảnh chính
         */
        @Schema(description = "URL hình ảnh chính", example = "https://example.com/image.jpg")
        private String mainImageUrl;

        /**
         * Danh sách đơn vị sản phẩm
         */
        @Schema(description = "Danh sách đơn vị sản phẩm")
        private List<ProductUnitSummary> units;
    }

    /**
     * Thông tin tóm tắt đơn vị sản phẩm cho danh sách
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thông tin tóm tắt đơn vị sản phẩm")
    public static class ProductUnitSummary {

        /**
         * ID đơn vị sản phẩm
         */
        @Schema(description = "ID đơn vị sản phẩm", example = "1")
        private Long id;

        /**
         * Mã vạch
         */
        @Schema(description = "Mã vạch", example = "1234567890123")
        private String barcode;

        /**
         * Tỷ lệ quy đổi so với đơn vị cơ bản
         */
        @Schema(description = "Tỷ lệ quy đổi so với đơn vị cơ bản", example = "1")
        private Integer conversionValue;

        /**
         * Có phải là đơn vị cơ bản không
         */
        @Schema(description = "Có phải là đơn vị cơ bản không", example = "true")
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

        /**
         * ID đơn vị tính
         */
        @Schema(description = "ID đơn vị tính", example = "1")
        private Long unitId;
    }

    /**
     * Thông tin phân trang
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thông tin phân trang")
    public static class PageInfo {

        /**
         * Trang hiện tại (bắt đầu từ 0)
         */
        @Schema(description = "Trang hiện tại", example = "0")
        private Integer currentPage;

        /**
         * Kích thước trang
         */
        @Schema(description = "Kích thước trang", example = "10")
        private Integer pageSize;

        /**
         * Tổng số phần tử
         */
        @Schema(description = "Tổng số phần tử", example = "100")
        private Long totalElements;

        /**
         * Tổng số trang
         */
        @Schema(description = "Tổng số trang", example = "10")
        private Integer totalPages;

        /**
         * Có trang đầu tiên không
         */
        @Schema(description = "Có phải trang đầu tiên", example = "true")
        private Boolean isFirst;

        /**
         * Có trang cuối cùng không
         */
        @Schema(description = "Có phải trang cuối cùng", example = "false")
        private Boolean isLast;

        /**
         * Có trang trước không
         */
        @Schema(description = "Có trang trước", example = "false")
        private Boolean hasPrevious;

        /**
         * Có trang tiếp theo không
         */
        @Schema(description = "Có trang tiếp theo", example = "true")
        private Boolean hasNext;
    }
}
