package iuh.fit.supermarket.dto.unit;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho yêu cầu cập nhật đơn vị tính
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật đơn vị tính")
public class UnitUpdateRequest {

    /**
     * Tên đơn vị tính
     */
    @Size(max = 50, message = "Tên đơn vị tính không được vượt quá 50 ký tự")
    @Schema(description = "Tên đơn vị tính", example = "Kilogram")
    private String name;

    /**
     * Trạng thái hoạt động
     */
    @Schema(description = "Trạng thái hoạt động", example = "true")
    private Boolean isActive;

    /**
     * Kiểm tra có dữ liệu cập nhật không
     *
     * @return true nếu có ít nhất một trường được cập nhật
     */
    public boolean hasUpdates() {
        return name != null || isActive != null;
    }
}
