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
 * DTO cho yêu cầu tạo phiếu kiểm kê kho
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo phiếu kiểm kê kho")
public class StocktakeCreateRequest {

    /**
     * Mã phiếu kiểm kê (tùy chọn - nếu không cung cấp sẽ tự động sinh)
     */
    @Schema(description = "Mã phiếu kiểm kê (tùy chọn - nếu không cung cấp sẽ tự động sinh)", example = "KK20240115001", required = false)
    private String stocktakeCode;

    /**
     * Ghi chú chung cho đợt kiểm kê
     */
    @Schema(description = "Ghi chú chung cho đợt kiểm kê", example = "Kiểm kê định kỳ tháng 1/2024")
    private String notes;

    /**
     * Trạng thái phiếu kiểm kê (mặc định là PENDING)
     */
    @Schema(description = "Trạng thái phiếu kiểm kê", example = "PENDING", allowableValues = { "PENDING",
            "COMPLETED" }, defaultValue = "PENDING")
    private StocktakeStatus status;

    /**
     * Danh sách chi tiết kiểm kê
     */
    @Valid
    @Schema(description = "Danh sách chi tiết kiểm kê")
    private List<StocktakeDetailCreateRequest> stocktakeDetails;

    /**
     * DTO cho yêu cầu tạo chi tiết kiểm kê
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Yêu cầu tạo chi tiết kiểm kê")
    public static class StocktakeDetailCreateRequest {

        /**
         * ID đơn vị sản phẩm
         */
        @NotNull(message = "ID đơn vị sản phẩm không được để trống")
        @Schema(description = "ID đơn vị sản phẩm", example = "1", required = true)
        private Long productUnitId;

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
    }

    /**
     * Constructor để tạo phiếu kiểm kê đơn giản
     *
     * @param notes ghi chú
     */
    public StocktakeCreateRequest(String notes) {
        this.notes = notes;
        this.status = StocktakeStatus.PENDING; // Mặc định là PENDING
    }

    /**
     * Constructor để tạo phiếu kiểm kê với trạng thái
     *
     * @param notes  ghi chú
     * @param status trạng thái phiếu kiểm kê
     */
    public StocktakeCreateRequest(String notes, StocktakeStatus status) {
        this.notes = notes;
        this.status = status;
    }

    /**
     * Constructor để tạo phiếu kiểm kê với mã code tùy chỉnh
     *
     * @param stocktakeCode mã phiếu kiểm kê
     * @param notes         ghi chú
     * @param status        trạng thái phiếu kiểm kê
     */
    public StocktakeCreateRequest(String stocktakeCode, String notes, StocktakeStatus status) {
        this.stocktakeCode = stocktakeCode;
        this.notes = notes;
        this.status = status;
    }
}
