package iuh.fit.supermarket.dto.stocktake;

import io.swagger.v3.oas.annotations.media.Schema;
import iuh.fit.supermarket.enums.StocktakeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho thông tin phiếu kiểm kê kho
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin phiếu kiểm kê kho")
public class StocktakeDto {

    /**
     * ID phiếu kiểm kê
     */
    @Schema(description = "ID phiếu kiểm kê", example = "1")
    private Integer stocktakeId;

    /**
     * Mã phiếu kiểm kê
     */
    @Schema(description = "Mã phiếu kiểm kê", example = "KK20240115001")
    private String stocktakeCode;

    /**
     * Trạng thái kiểm kê
     */
    @Schema(description = "Trạng thái kiểm kê", example = "PENDING")
    private StocktakeStatus status;

    /**
     * Ghi chú chung cho đợt kiểm kê
     */
    @Schema(description = "Ghi chú chung cho đợt kiểm kê", example = "Kiểm kê định kỳ tháng 1/2024")
    private String notes;

    /**
     * Thời điểm hoàn tất kiểm kê
     */
    @Schema(description = "Thời điểm hoàn tất kiểm kê", example = "2024-01-15T16:30:00")
    private LocalDateTime completedAt;

    /**
     * Thời gian tạo
     */
    @Schema(description = "Thời gian tạo", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật
     */
    @Schema(description = "Thời gian cập nhật", example = "2024-01-15T16:30:00")
    private LocalDateTime updatedAt;

    /**
     * Thông tin nhân viên tạo phiếu
     */
    @Schema(description = "Thông tin nhân viên tạo phiếu")
    private EmployeeInfo createdBy;

    /**
     * Thông tin nhân viên hoàn thành phiếu
     */
    @Schema(description = "Thông tin nhân viên hoàn thành phiếu")
    private EmployeeInfo completedBy;

    /**
     * Danh sách chi tiết kiểm kê
     */
    @Schema(description = "Danh sách chi tiết kiểm kê")
    private List<StocktakeDetailDto> stocktakeDetails;

    /**
     * Thống kê tổng quan
     */
    @Schema(description = "Thống kê tổng quan")
    private StocktakeSummary summary;

    /**
     * DTO đơn giản cho thông tin nhân viên
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thông tin nhân viên")
    public static class EmployeeInfo {

        /**
         * ID nhân viên
         */
        @Schema(description = "ID nhân viên", example = "1")
        private Integer employeeId;

        /**
         * Tên nhân viên
         */
        @Schema(description = "Tên nhân viên", example = "Nguyễn Văn A")
        private String name;

        /**
         * Email nhân viên
         */
        @Schema(description = "Email nhân viên", example = "nguyenvana@example.com")
        private String email;
    }

    /**
     * DTO cho thống kê tổng quan phiếu kiểm kê
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thống kê tổng quan phiếu kiểm kê")
    public static class StocktakeSummary {

        /**
         * Tổng số loại sản phẩm kiểm kê
         */
        @Schema(description = "Tổng số loại sản phẩm kiểm kê", example = "150")
        private Long totalItems;

        /**
         * Số loại sản phẩm có chênh lệch
         */
        @Schema(description = "Số loại sản phẩm có chênh lệch", example = "25")
        private Long itemsWithDifference;

        /**
         * Tổng chênh lệch dương (thừa)
         */
        @Schema(description = "Tổng chênh lệch dương (thừa)", example = "120")
        private Integer totalPositiveDifference;

        /**
         * Tổng chênh lệch âm (thiếu)
         */
        @Schema(description = "Tổng chênh lệch âm (thiếu)", example = "-80")
        private Integer totalNegativeDifference;

        /**
         * Chênh lệch ròng
         */
        @Schema(description = "Chênh lệch ròng", example = "40")
        private Integer netDifference;
    }
}
