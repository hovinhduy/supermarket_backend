package iuh.fit.supermarket.dto.stocktake;

import io.swagger.v3.oas.annotations.media.Schema;
import iuh.fit.supermarket.enums.StocktakeStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho yêu cầu cập nhật phiếu kiểm kê kho
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu cập nhật phiếu kiểm kê kho")
public class StocktakeUpdateRequest {

    /**
     * Trạng thái kiểm kê mới
     */
    @Schema(description = "Trạng thái kiểm kê mới", example = "COMPLETED")
    private StocktakeStatus status;

    /**
     * Ghi chú chung cho đợt kiểm kê
     */
    @Schema(description = "Ghi chú chung cho đợt kiểm kê", example = "Kiểm kê định kỳ tháng 1/2024 - Đã hoàn thành")
    private String notes;

    /**
     * Danh sách chi tiết kiểm kê cập nhật (tùy chọn)
     */
    @Valid
    @Schema(description = "Danh sách chi tiết kiểm kê cập nhật")
    private List<StocktakeDetailUpdateRequest> stocktakeDetails;

    /**
     * DTO cho yêu cầu cập nhật chi tiết kiểm kê
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Yêu cầu cập nhật chi tiết kiểm kê")
    public static class StocktakeDetailUpdateRequest {

        /**
         * ID đơn vị sản phẩm để xác định chi tiết cần cập nhật
         * (Có thể dùng productUnitId hoặc stocktakeDetailId)
         */
        @Schema(description = "ID đơn vị sản phẩm để cập nhật", example = "1")
        private Long productUnitId;

        /**
         * ID chi tiết kiểm kê (để xác định bản ghi cần cập nhật)
         * (Tùy chọn - có thể dùng productUnitId thay thế)
         */
        @Schema(description = "ID chi tiết kiểm kê", example = "1")
        private Integer stocktakeDetailId;

        /**
         * Số lượng thực tế đếm được (cập nhật)
         */
        @PositiveOrZero(message = "Số lượng thực tế đếm được phải >= 0")
        @Schema(description = "Số lượng thực tế đếm được", example = "98")
        private Integer quantityCounted;

        /**
         * Ghi chú lý do chênh lệch (cập nhật)
         */
        @Schema(description = "Ghi chú lý do chênh lệch", example = "Đã kiểm tra lại - hàng bị hỏng do vận chuyển")
        private String reason;
    }

    /**
     * Constructor để cập nhật trạng thái phiếu kiểm kê
     *
     * @param status trạng thái mới
     */
    public StocktakeUpdateRequest(StocktakeStatus status) {
        this.status = status;
    }

    /**
     * Constructor để cập nhật ghi chú
     * 
     * @param notes ghi chú mới
     */
    public StocktakeUpdateRequest(String notes) {
        this.notes = notes;
    }
}
