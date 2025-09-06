package iuh.fit.supermarket.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho response upload hình ảnh sản phẩm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageUploadResponse {

    /**
     * ID của hình ảnh vừa được tạo
     */
    private Integer imageId;

    /**
     * URL của hình ảnh đã upload
     */
    private String imageUrl;

    /**
     * Thông báo kết quả
     */
    private String message;

    /**
     * Thứ tự sắp xếp được gán
     */
    private Integer sortOrder;

    /**
     * Kích thước file (bytes)
     */
    private Long fileSize;

    /**
     * Loại file
     */
    private String contentType;

    /**
     * Constructor cho trường hợp thành công
     */
    public ProductImageUploadResponse(Integer imageId, String imageUrl, String message, Integer sortOrder) {
        this.imageId = imageId;
        this.imageUrl = imageUrl;
        this.message = message;
        this.sortOrder = sortOrder;
    }
}

