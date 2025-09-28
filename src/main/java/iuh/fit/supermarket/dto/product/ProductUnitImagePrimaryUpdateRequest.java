package iuh.fit.supermarket.dto.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho yêu cầu thay đổi ảnh chính của đơn vị sản phẩm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUnitImagePrimaryUpdateRequest {
    
    /**
     * ID của đơn vị sản phẩm
     */
    @NotNull(message = "ID đơn vị sản phẩm không được để trống")
    @Positive(message = "ID đơn vị sản phẩm phải là số dương")
    private Long productUnitId;
    
    /**
     * ID của ảnh sản phẩm sẽ được đặt làm ảnh chính
     */
    @NotNull(message = "ID ảnh sản phẩm không được để trống")
    @Positive(message = "ID ảnh sản phẩm phải là số dương")
    private Integer productImageId;
}
