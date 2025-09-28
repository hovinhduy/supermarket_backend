package iuh.fit.supermarket.dto.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * DTO cho yêu cầu upload ảnh mới cho đơn vị sản phẩm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUnitImageUploadRequest {
    
    /**
     * ID của đơn vị sản phẩm
     */
    @NotNull(message = "ID đơn vị sản phẩm không được để trống")
    @Positive(message = "ID đơn vị sản phẩm phải là số dương")
    private Long productUnitId;
    
    /**
     * File ảnh cần upload
     */
    @NotNull(message = "File ảnh không được để trống")
    private MultipartFile imageFile;
    
    /**
     * Văn bản thay thế cho ảnh
     */
    private String imageAlt;
    
    /**
     * Thứ tự hiển thị
     */
    private Integer displayOrder;
    
    /**
     * Đánh dấu có phải ảnh chính không
     */
    private Boolean isPrimary = false;
}
