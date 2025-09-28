package iuh.fit.supermarket.dto.unit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho thông tin đơn vị tính
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin đơn vị tính")
public class UnitDto {

    /**
     * ID đơn vị tính
     */
    @Schema(description = "ID đơn vị tính", example = "1")
    private Long id;

    /**
     * Tên đơn vị tính
     */
    @Schema(description = "Tên đơn vị tính", example = "Kilogram")
    private String name;

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
     * Thời gian tạo
     */
    @Schema(description = "Thời gian tạo")
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật
     */
    @Schema(description = "Thời gian cập nhật")
    private LocalDateTime updatedAt;

    /**
     * Số lượng sản phẩm đang sử dụng đơn vị này
     */
    @Schema(description = "Số lượng sản phẩm đang sử dụng đơn vị này", example = "5")
    private Long usageCount;

    /**
     * Có thể xóa hay không
     */
    @Schema(description = "Có thể xóa hay không", example = "true")
    private Boolean canDelete;
}
