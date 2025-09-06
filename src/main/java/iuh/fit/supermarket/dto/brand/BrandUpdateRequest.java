package iuh.fit.supermarket.dto.brand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho yêu cầu cập nhật thương hiệu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật thương hiệu")
public class BrandUpdateRequest {

    /**
     * Tên thương hiệu
     */
    @NotBlank(message = "Tên thương hiệu không được để trống")
    @Size(max = 255, message = "Tên thương hiệu không được vượt quá 255 ký tự")
    @Schema(description = "Tên thương hiệu", example = "Samsung Electronics", required = true)
    private String name;

    /**
     * Mã thương hiệu
     */
    @Size(max = 50, message = "Mã thương hiệu không được vượt quá 50 ký tự")
    @Schema(description = "Mã thương hiệu", example = "BR0001")
    private String brandCode;

    /**
     * URL logo thương hiệu
     */
    @Size(max = 500, message = "URL logo không được vượt quá 500 ký tự")
    @Schema(description = "URL logo thương hiệu", example = "https://example.com/new-logo.png")
    private String logoUrl;

    /**
     * Mô tả thương hiệu
     */
    @Schema(description = "Mô tả thương hiệu", example = "Thương hiệu điện tử và công nghệ hàng đầu")
    private String description;

    /**
     * Trạng thái hoạt động
     */
    @Schema(description = "Trạng thái hoạt động", example = "true")
    private Boolean isActive;
}