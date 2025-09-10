package iuh.fit.supermarket.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;

/**
 * DTO cho request upload hình ảnh sản phẩm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageUploadRequest {

    /**
     * ID sản phẩm (bắt buộc)
     */
    @NotNull(message = "ID sản phẩm không được để trống")
    private Long productId;

    /**
     * ID biến thể (tùy chọn - nếu có thì ảnh thuộc về biến thể cụ thể)
     */
    private Long variantId;

    /**
     * Văn bản thay thế cho hình ảnh
     */
    private String imageAlt;

    /**
     * Thứ tự sắp xếp (tùy chọn - nếu không có sẽ tự động đặt)
     */
    private Integer sortOrder;

    /**
     * File ảnh cần upload
     */
    @NotNull(message = "File ảnh không được để trống")
    private MultipartFile imageFile;
}
