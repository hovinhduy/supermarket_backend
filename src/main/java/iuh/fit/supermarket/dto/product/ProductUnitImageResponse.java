package iuh.fit.supermarket.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho phản hồi danh sách ảnh của đơn vị sản phẩm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUnitImageResponse {
    
    /**
     * ID của đơn vị sản phẩm
     */
    private Long productUnitId;
    
    /**
     * Tên đơn vị sản phẩm
     */
    private String productUnitName;
    
    /**
     * Danh sách ảnh của đơn vị sản phẩm
     */
    private List<ProductUnitImageDto> images;
    
    /**
     * Ảnh chính của đơn vị sản phẩm
     */
    private ProductUnitImageDto primaryImage;
    
    /**
     * Tổng số ảnh
     */
    private Integer totalImages;
}
