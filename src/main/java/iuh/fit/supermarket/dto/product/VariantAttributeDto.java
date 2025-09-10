package iuh.fit.supermarket.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho thuộc tính biến thể sản phẩm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VariantAttributeDto {

    /**
     * ID của liên kết thuộc tính biến thể
     */
    private Long id;

    /**
     * ID của biến thể sản phẩm
     */
    private Long variantId;

    /**
     * ID của giá trị thuộc tính
     */
    private Long attributeValueId;

    /**
     * Tên thuộc tính (màu sắc, kích thước...)
     */
    private String attributeName;

    /**
     * Giá trị thuộc tính (đỏ, L...)
     */
    private String attributeValue;

    /**
     * Mô tả giá trị thuộc tính
     */
    private String attributeValueDescription;

    /**
     * Thời gian tạo
     */
    private LocalDateTime createdDate;

    /**
     * Constructor đơn giản chỉ với ID
     */
    public VariantAttributeDto(Long variantId, Long attributeValueId) {
        this.variantId = variantId;
        this.attributeValueId = attributeValueId;
    }
}
