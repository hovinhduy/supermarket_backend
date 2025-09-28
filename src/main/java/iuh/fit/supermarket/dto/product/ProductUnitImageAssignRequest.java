package iuh.fit.supermarket.dto.product;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho yêu cầu gán ảnh từ sản phẩm gốc vào đơn vị sản phẩm
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductUnitImageAssignRequest {
    
    /**
     * ID của đơn vị sản phẩm
     */
    @NotNull(message = "ID đơn vị sản phẩm không được để trống")
    @Positive(message = "ID đơn vị sản phẩm phải là số dương")
    private Long productUnitId;
    
    /**
     * Danh sách ID của các ảnh sản phẩm cần gán
     */
    @NotNull(message = "Danh sách ID ảnh không được để trống")
    private List<Integer> productImageIds;
    
    /**
     * ID của ảnh được đặt làm ảnh chính (tùy chọn)
     */
    private Integer primaryImageId;
}
