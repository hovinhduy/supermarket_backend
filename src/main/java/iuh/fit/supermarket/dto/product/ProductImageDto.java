package iuh.fit.supermarket.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO đại diện cho thông tin hình ảnh sản phẩm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageDto {

    /**
     * ID của hình ảnh
     */
    private Integer imageId;

    /**
     * URL của hình ảnh
     */
    private String imageUrl;

    /**
     * Văn bản thay thế cho hình ảnh
     */
    private String imageAlt;

    /**
     * Thứ tự sắp xếp
     */
    private Integer sortOrder;

    /**
     * Thời gian tạo
     */
    private LocalDateTime createdAt;

    /**
     * ID sản phẩm
     */
    private Long productId;

    /**
     * ID biến thể (nếu có)
     */
    private Integer variantId;
}
