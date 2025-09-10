package iuh.fit.supermarket.dto.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO request cho việc tạo giá trị thuộc tính mới
 */
@Data
public class AttributeValueCreateRequest {

    /**
     * Giá trị cụ thể của thuộc tính
     */
    @NotBlank(message = "Giá trị thuộc tính không được để trống")
    private String value;

    /**
     * Mô tả về giá trị thuộc tính (tùy chọn)
     */
    private String description;

    /**
     * ID của thuộc tính mà giá trị này thuộc về
     */
    @NotNull(message = "ID thuộc tính không được để trống")
    private Long attributeId;
}
