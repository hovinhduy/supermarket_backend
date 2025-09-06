package iuh.fit.supermarket.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho yêu cầu tạo danh mục mới
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreateRequest {
    
    /**
     * Tên danh mục (bắt buộc, độ dài 2-100 ký tự)
     */
    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(min = 2, max = 100, message = "Tên danh mục phải từ 2 đến 100 ký tự")
    private String name;
    
    /**
     * Mô tả danh mục (không bắt buộc)
     */
    private String description;
    
    /**
     * ID của danh mục cha (không bắt buộc)
     */
    private Integer parentId;
}