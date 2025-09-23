package iuh.fit.supermarket.dto.price;

import iuh.fit.supermarket.enums.PriceType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho yêu cầu cập nhật trạng thái bảng giá
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceStatusUpdateRequest {

    /**
     * Trạng thái mới của bảng giá
     */
    @NotNull(message = "Trạng thái không được để trống")
    private PriceType status;

    /**
     * Lý do thay đổi trạng thái
     */
    private String reason;
}
