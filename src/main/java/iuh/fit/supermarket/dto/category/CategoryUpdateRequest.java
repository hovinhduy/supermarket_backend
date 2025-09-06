package iuh.fit.supermarket.dto.category;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho yêu cầu cập nhật danh mục
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryUpdateRequest {
    
    /**
     * Tên danh mục (độ dài 2-100 ký tự)
     */
    @Size(min = 2, max = 100, message = "Tên danh mục phải từ 2 đến 100 ký tự")
    private String name;
    
    /**
     * Mô tả danh mục
     */
    private String description;
    
    /**
     * Trạng thái hoạt động
     */
    private Boolean isActive;
    
    /**
     * ID của danh mục cha
     */
    private Integer parentId;
}