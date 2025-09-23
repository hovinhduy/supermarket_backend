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
     * ID của biến thể sản phẩm
     */
    private Long variantId;

    /**
     * Mã biến thể sản phẩm
     */
    private String variantCode;

    /**
     * Tên biến thể sản phẩm
     */
    private String variantName;

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
