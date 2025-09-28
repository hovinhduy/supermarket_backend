package iuh.fit.supermarket.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho yêu cầu cập nhật sản phẩm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật sản phẩm")
public class ProductUpdateRequest {

    /**
     * Tên sản phẩm
     */
    @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    @Schema(description = "Tên sản phẩm", example = "Smartphone Samsung Galaxy S24 Plus")
    private String name;

    /**
     * Mô tả sản phẩm
     */
    @Schema(description = "Mô tả sản phẩm", example = "Điện thoại thông minh cao cấp với camera chất lượng cao và màn hình lớn")
    private String description;

    /**
     * ID thương hiệu
     */
    @Positive(message = "ID thương hiệu phải là số dương")
    @Schema(description = "ID thương hiệu", example = "1")
    private Integer brandId;

    /**
     * ID danh mục
     */
    @Positive(message = "ID danh mục phải là số dương")
    @Schema(description = "ID danh mục", example = "1")
    private Integer categoryId;

    /**
     * Có tích điểm thưởng không
     */
    @Schema(description = "Có tích điểm thưởng không", example = "true")
    private Boolean isRewardPoint;

    /**
     * Trạng thái hoạt động
     */
    @Schema(description = "Trạng thái hoạt động", example = "true")
    private Boolean isActive;
}
