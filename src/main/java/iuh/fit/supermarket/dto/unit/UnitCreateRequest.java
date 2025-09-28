package iuh.fit.supermarket.dto.unit;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho yêu cầu tạo đơn vị tính mới
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo đơn vị tính mới")
public class UnitCreateRequest {

    /**
     * Tên đơn vị tính (duy nhất)
     */
    @NotBlank(message = "Tên đơn vị tính không được để trống")
    @Size(max = 50, message = "Tên đơn vị tính không được vượt quá 50 ký tự")
    @Schema(description = "Tên đơn vị tính", example = "Kilogram", required = true)
    private String name;

    /**
     * Trạng thái hoạt động
     */
    @Schema(description = "Trạng thái hoạt động", example = "true")
    private Boolean isActive = true;

    /**
     * Constructor với tên đơn vị tính
     */
    public UnitCreateRequest(String name) {
        this.name = name;
        this.isActive = true;
    }
}
