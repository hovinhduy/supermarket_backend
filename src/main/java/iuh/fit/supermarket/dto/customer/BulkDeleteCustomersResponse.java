package iuh.fit.supermarket.dto.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho response của việc xóa nhiều khách hàng cùng lúc
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response cho việc xóa nhiều khách hàng cùng lúc")
public class BulkDeleteCustomersResponse {

    /**
     * Tổng số khách hàng được yêu cầu xóa
     */
    @Schema(description = "Tổng số khách hàng được yêu cầu xóa", example = "5")
    private int totalRequested;

    /**
     * Số lượng khách hàng xóa thành công
     */
    @Schema(description = "Số lượng khách hàng xóa thành công", example = "4")
    private int successCount;

    /**
     * Số lượng khách hàng xóa thất bại
     */
    @Schema(description = "Số lượng khách hàng xóa thất bại", example = "1")
    private int failedCount;

    /**
     * Danh sách ID khách hàng xóa thành công
     */
    @Schema(description = "Danh sách ID khách hàng xóa thành công", example = "[1, 2, 3, 4]")
    private List<Integer> successIds;

    /**
     * Danh sách ID khách hàng xóa thất bại
     */
    @Schema(description = "Danh sách ID khách hàng xóa thất bại", example = "[5]")
    private List<Integer> failedIds;

    /**
     * Danh sách thông báo lỗi chi tiết
     */
    @Schema(description = "Danh sách thông báo lỗi chi tiết", example = "[\"Khách hàng với ID 5 không tồn tại\"]")
    private List<String> errors;

    /**
     * Kiểm tra xem có lỗi nào không
     */
    public boolean hasErrors() {
        return failedCount > 0;
    }

    /**
     * Kiểm tra xem tất cả có thành công không
     */
    public boolean isAllSuccess() {
        return failedCount == 0 && successCount > 0;
    }

    /**
     * Lấy tỷ lệ thành công (%)
     */
    public double getSuccessRate() {
        if (totalRequested == 0) {
            return 0.0;
        }
        return (double) successCount / totalRequested * 100;
    }

    /**
     * Tạo response cho trường hợp tất cả thành công
     */
    public static BulkDeleteCustomersResponse allSuccess(List<Integer> successIds) {
        return BulkDeleteCustomersResponse.builder()
                .totalRequested(successIds.size())
                .successCount(successIds.size())
                .failedCount(0)
                .successIds(successIds)
                .failedIds(List.of())
                .errors(List.of())
                .build();
    }

    /**
     * Tạo response cho trường hợp có lỗi
     */
    public static BulkDeleteCustomersResponse withErrors(
            int totalRequested,
            List<Integer> successIds,
            List<Integer> failedIds,
            List<String> errors) {
        return BulkDeleteCustomersResponse.builder()
                .totalRequested(totalRequested)
                .successCount(successIds.size())
                .failedCount(failedIds.size())
                .successIds(successIds)
                .failedIds(failedIds)
                .errors(errors)
                .build();
    }
}
