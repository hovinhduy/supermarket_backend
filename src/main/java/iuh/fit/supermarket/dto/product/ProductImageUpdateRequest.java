package iuh.fit.supermarket.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;

/**
 * DTO cho yêu cầu cập nhật thông tin hình ảnh sản phẩm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageUpdateRequest {
    
    /**
     * Văn bản thay thế cho hình ảnh
     */
    private String imageAlt;
    
    /**
     * Thứ tự sắp xếp
     */
    @Min(value = 0, message = "Sort order phải >= 0")
    private Integer sortOrder;
}
