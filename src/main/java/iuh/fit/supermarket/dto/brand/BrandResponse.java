package iuh.fit.supermarket.dto.brand;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho phản hồi thông tin thương hiệu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin thương hiệu")
public class BrandResponse {

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

    /**
     * Mô tả thương hiệu
     */
    @Schema(description = "Mô tả thương hiệu", example = "Thương hiệu điện tử hàng đầu thế giới")
    private String description;

    /**
     * Trạng thái hoạt động
     */
    @Schema(description = "Trạng thái hoạt động", example = "true")
    private Boolean isActive;

    /**
     * Thời gian tạo
     */
    @Schema(description = "Thời gian tạo", example = "2024-01-01T10:00:00")
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật
     */
    @Schema(description = "Thời gian cập nhật", example = "2024-01-01T10:00:00")
    private LocalDateTime updatedAt;

    /**
     * Số lượng sản phẩm của thương hiệu
     */
    @Schema(description = "Số lượng sản phẩm", example = "25")
    private Integer productCount;
}