package iuh.fit.supermarket.dto.price;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO cho thông tin chi tiết giá sản phẩm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceDetailDto {

    /**
     * ID của chi tiết giá
     */
    private Long priceDetailId;

    /**
     * ID của đơn vị sản phẩm
     */
    private Long productUnitId;

    /**
     * Mã đơn vị sản phẩm
     */
    private String productUnitCode;

    /**
     * Tên đơn vị sản phẩm
     */
    private String productUnitName;

    /**
     * Mã vạch của sản phẩm
     */
    private String barcode;

    /**
     * Giá bán
     */
    private BigDecimal salePrice;

    /**
     * Thời gian tạo
     */
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật
     */
    private LocalDateTime updatedAt;
}
