package iuh.fit.supermarket.dto.product;

import lombok.Data;

import java.util.List;

/**
 * DTO cho phản hồi khi tạo sản phẩm thành công
 */
@Data
public class ProductCreateResponse {

    /**
     * ID sản phẩm đã tạo
     */
    private Long id;

    /**
     * Tên sản phẩm đã tạo
     */
    private String name;

    /**
     * Số lượng SKU đã tạo
     */
    private Integer skuCount;

    /**
     * Số lượng biến thể đã tạo
     */
    private Integer variantCount;

    /**
     * Thông báo kết quả
     */
    private String message;

    /**
     * Danh sách biến thể đã tạo
     */
    private List<ProductVariantDto> variants;
}
