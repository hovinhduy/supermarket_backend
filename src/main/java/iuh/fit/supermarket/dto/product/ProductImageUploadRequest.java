package iuh.fit.supermarket.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

/**
 * DTO cho yêu cầu upload hình ảnh sản phẩm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageUploadRequest {
    
    /**
     * ID của sản phẩm
     */
    @NotNull(message = "Product ID không được để trống")
    private Long productId;
    
    /**
     * ID của biến thể (tùy chọn)
     */
    private Long variantId;
    
    /**
     * File hình ảnh
     */
    @NotNull(message = "File ảnh không được để trống")
    private MultipartFile imageFile;
    
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
