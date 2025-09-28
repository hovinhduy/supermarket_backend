package iuh.fit.supermarket.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho thông tin ảnh của đơn vị sản phẩm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUnitImageDto {
    
    /**
     * ID của mapping
     */
    private Long id;
    
    /**
     * Thứ tự hiển thị của hình ảnh
     */
    private Integer displayOrder;
    
    /**
     * Đánh dấu hình ảnh chính
     */
    private Boolean isPrimary;
    
    /**
     * Trạng thái hoạt động
     */
    private Boolean isActive;
    
    /**
     * Thời gian tạo mapping
     */
    private LocalDateTime createdAt;
    
    /**
     * ID của đơn vị sản phẩm
     */
    private Long productUnitId;
    
    /**
     * Thông tin hình ảnh sản phẩm
     */
    private ProductImageDto productImage;
}
