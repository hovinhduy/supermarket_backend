package iuh.fit.supermarket.dto.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho yêu cầu tạo mới sản phẩm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo mới sản phẩm")
public class ProductCreateRequest {

    /**
     * Mã sản phẩm (tùy chọn - tự động sinh nếu không nhập)
     */
    @Size(max = 50, message = "Mã sản phẩm không được vượt quá 50 ký tự")
    @Schema(description = "Mã sản phẩm (tùy chọn - tự động sinh nếu không nhập)", example = "SP001")
    private String code;

    /**
     * Tên sản phẩm
     */
    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    @Schema(description = "Tên sản phẩm", example = "Coca Cola", required = true)
    private String name;

    /**
     * Mô tả sản phẩm
     */
    @Schema(description = "Mô tả sản phẩm", example = "Coca Cola là một loại đồ uống có ga")
    private String description;

    /**
     * ID thương hiệu (tùy chọn)
     */
    @Positive(message = "ID thương hiệu phải là số dương")
    @Schema(description = "ID thương hiệu", example = "1")
    private Integer brandId;

    /**
     * ID danh mục
     */
    @NotNull(message = "ID danh mục không được để trống")
    @Positive(message = "ID danh mục phải là số dương")
    @Schema(description = "ID danh mục", example = "1", required = true)
    private Integer categoryId;

    /**
     * Có tích điểm thưởng không
     */
    @Schema(description = "Có tích điểm thưởng không", example = "true")
    private Boolean isRewardPoint = false;

    /**
     * Trạng thái hoạt động
     */
    @Schema(description = "Trạng thái hoạt động", example = "true")
    private Boolean isActive = true;

    /**
     * Danh sách đơn vị sản phẩm (bắt buộc phải có ít nhất 1 đơn vị cơ bản)
     */
    @NotEmpty(message = "Danh sách đơn vị sản phẩm không được để trống")
    @Valid
    @Schema(description = "Danh sách đơn vị sản phẩm", required = true)
    private List<ProductUnitRequest> units;

    /**
     * Constructor với tên sản phẩm
     */
    public ProductCreateRequest(String name) {
        this.name = name;
        this.isActive = true;
        this.isRewardPoint = false;
    }
}
