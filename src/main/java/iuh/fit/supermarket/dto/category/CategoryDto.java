package iuh.fit.supermarket.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho thông tin danh mục sản phẩm
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {
    private Integer id;
    private String name;
    private String description;
    private Boolean isActive;
    private Integer level;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer parentId;
    private String parentName;
    private List<CategoryDto> children;
}