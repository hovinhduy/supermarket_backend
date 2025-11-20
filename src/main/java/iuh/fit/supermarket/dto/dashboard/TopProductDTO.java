package iuh.fit.supermarket.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO cho thông tin sản phẩm bán chạy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductDTO {
    
    /**
     * ID của đơn vị sản phẩm
     */
    private Long productUnitId;
    
    /**
     * ID sản phẩm
     */
    private Long productId;
    
    /**
     * Tên sản phẩm
     */
    private String productName;
    
    /**
     * Tên đơn vị
     */
    private String unitName;
    
    /**
     * Mã vạch (barcode)
     */
    private String barcode;
    
    /**
     * Tổng số lượng đã bán
     */
    private Long totalQuantitySold;
    
    /**
     * Tổng doanh thu từ sản phẩm này
     */
    private BigDecimal totalRevenue;
    
    /**
     * Thứ hạng (1-5)
     */
    private Integer rank;
}
