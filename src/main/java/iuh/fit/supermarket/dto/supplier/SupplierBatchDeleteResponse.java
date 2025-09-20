package iuh.fit.supermarket.dto.supplier;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho response của batch delete suppliers
 */
@Data
@NoArgsConstructor
@Schema(description = "Response cho việc xóa nhiều nhà cung cấp cùng lúc")
public class SupplierBatchDeleteResponse {

    /**
     * Số lượng nhà cung cấp đã xóa thành công
     */
    @Schema(description = "Số lượng nhà cung cấp đã xóa thành công", example = "5")
    private int deletedCount;

    /**
     * Danh sách ID của các nhà cung cấp không thể xóa
     */
    @Schema(description = "Danh sách ID của các nhà cung cấp không thể xóa", example = "[6, 7]")
    private List<Integer> failedIds;

    /**
     * Danh sách lý do thất bại tương ứng với failedIds
     */
    @Schema(description = "Danh sách lý do thất bại tương ứng với failedIds", example = "[\"Nhà cung cấp không tồn tại\", \"Nhà cung cấp đã bị xóa\"]")
    private List<String> failedReasons;

    /**
     * Tổng số ID được yêu cầu xóa
     */
    @Schema(description = "Tổng số ID được yêu cầu xóa", example = "7")
    private int totalRequested;

    /**
     * Constructor với các tham số cơ bản
     */
    public SupplierBatchDeleteResponse(int deletedCount, List<Integer> failedIds) {
        this.deletedCount = deletedCount;
        this.failedIds = failedIds != null ? failedIds : List.of();
        this.failedReasons = List.of();
        this.totalRequested = deletedCount + this.failedIds.size();
    }

    /**
     * Constructor đầy đủ
     */
    public SupplierBatchDeleteResponse(int deletedCount, List<Integer> failedIds,
            List<String> failedReasons, int totalRequested) {
        this.deletedCount = deletedCount;
        this.failedIds = failedIds != null ? failedIds : List.of();
        this.failedReasons = failedReasons != null ? failedReasons : List.of();
        this.totalRequested = totalRequested;
    }

    /**
     * Kiểm tra có lỗi không
     * 
     * @return true nếu có ID thất bại
     */
    public boolean hasFailures() {
        return failedIds != null && !failedIds.isEmpty();
    }

    /**
     * Kiểm tra có thành công hoàn toàn không
     * 
     * @return true nếu tất cả đều thành công
     */
    public boolean isCompleteSuccess() {
        return !hasFailures() && deletedCount > 0;
    }

    /**
     * Lấy tỷ lệ thành công
     * 
     * @return tỷ lệ thành công (0.0 - 1.0)
     */
    public double getSuccessRate() {
        if (totalRequested == 0) {
            return 0.0;
        }
        return (double) deletedCount / totalRequested;
    }

    /**
     * Tạo message mô tả kết quả
     * 
     * @return message mô tả
     */
    public String getResultMessage() {
        if (isCompleteSuccess()) {
            return String.format("Đã xóa thành công %d nhà cung cấp", deletedCount);
        } else if (deletedCount == 0) {
            return String.format("Không thể xóa nhà cung cấp nào. %d ID thất bại", failedIds.size());
        } else {
            return String.format("Đã xóa thành công %d/%d nhà cung cấp. %d ID thất bại",
                    deletedCount, totalRequested, failedIds.size());
        }
    }
}
