package iuh.fit.supermarket.dto.stocktake;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho yêu cầu tạo chi tiết kiểm kê kho (standalone)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo chi tiết kiểm kê kho")
public class StocktakeDetailCreateRequest {

    /**
     * ID phiếu kiểm kê
     */
    @NotNull(message = "ID phiếu kiểm kê không được để trống")
    @Schema(description = "ID phiếu kiểm kê", example = "1", required = true)
    private Integer stocktakeId;

    /**
     * ID biến thể sản phẩm
     */
    @NotNull(message = "ID biến thể sản phẩm không được để trống")
    @Schema(description = "ID biến thể sản phẩm", example = "1", required = true)
    private Long variantId;

    /**
     * Số lượng thực tế đếm được
     */
    @NotNull(message = "Số lượng thực tế đếm được không được để trống")
    @PositiveOrZero(message = "Số lượng thực tế đếm được phải >= 0")
    @Schema(description = "Số lượng thực tế đếm được", example = "95", required = true)
    private Integer quantityCounted;

    /**
     * Ghi chú lý do chênh lệch (nếu có)
     */
    @Schema(description = "Ghi chú lý do chênh lệch", example = "Hàng bị hỏng do vận chuyển")
    private String reason;

    /**
     * Constructor đơn giản
     * 
     * @param stocktakeId ID phiếu kiểm kê
     * @param variantId ID biến thể sản phẩm
     * @param quantityCounted số lượng thực tế đếm được
     */
    public StocktakeDetailCreateRequest(Integer stocktakeId, Long variantId, Integer quantityCounted) {
        this.stocktakeId = stocktakeId;
        this.variantId = variantId;
        this.quantityCounted = quantityCounted;
    }
}
