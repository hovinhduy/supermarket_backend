package iuh.fit.supermarket.dto.favorite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho response thông tin sản phẩm yêu thích
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerFavoriteResponse {

    /**
     * ID của đơn vị sản phẩm
     */
    private Long productUnitId;

    /**
     * Mã sản phẩm
     */
    private String productCode;

    /**
     * Tên sản phẩm
     */
    private String productName;

    /**
     * Tên đơn vị tính
     */
    private String unitName;

    /**
     * Giá hiện tại
     */
    private Double currentPrice;

    /**
     * URL hình ảnh chính
     */
    private String imageUrl;

    /**
     * Trạng thái còn hàng
     */
    private Boolean inStock;

    /**
     * Thời gian thêm vào danh sách yêu thích
     */
    private LocalDateTime createdAt;
}
