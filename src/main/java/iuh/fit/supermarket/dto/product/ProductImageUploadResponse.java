package iuh.fit.supermarket.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho phản hồi upload hình ảnh sản phẩm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageUploadResponse {

    /**
     * ID của hình ảnh đã upload
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
     * Thứ tự sắp xếp
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
     * Trạng thái upload
     */
    private boolean success = true;

    /**
     * Constructor for successful upload
     */
    public ProductImageUploadResponse(Integer imageId, String imageUrl, String message,
            Integer sortOrder, Long fileSize, String contentType) {
        this.imageId = imageId;
        this.imageUrl = imageUrl;
        this.message = message;
        this.sortOrder = sortOrder;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.success = true;
    }
}
