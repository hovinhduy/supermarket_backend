package iuh.fit.supermarket.dto.promotion;

import iuh.fit.supermarket.enums.PromotionStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho request cập nhật trạng thái bulk promotion
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkUpdateStatusRequest {
    
    @NotEmpty(message = "Danh sách ID promotion không được để trống")
    private List<Long> promotionIds;
    
    @NotNull(message = "Trạng thái không được để trống")
    private PromotionStatus status;
}
