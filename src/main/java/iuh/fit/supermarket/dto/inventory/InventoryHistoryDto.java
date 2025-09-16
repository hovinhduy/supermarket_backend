package iuh.fit.supermarket.dto.inventory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho thông tin lịch sử thay đổi kho hàng
 * Chứa thông tin tổng hợp về các thay đổi tồn kho theo thời gian
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông tin lịch sử thay đổi kho hàng")
public class InventoryHistoryDto {

    /**
     * Thời gian thay đổi
     */
    @Schema(description = "Thời gian thay đổi kho hàng", example = "2024-01-15T10:30:00")
    private LocalDateTime time;

    /**
     * Số lượng thực tế sau khi thay đổi
     */
    @Schema(description = "Số lượng tồn kho thực tế sau khi thay đổi", example = "150")
    private Integer actualQuantity;

    /**
     * Tổng chênh lệch (có thể âm hoặc dương)
     */
    @Schema(description = "Tổng chênh lệch số lượng (dương = tăng, âm = giảm)", example = "10")
    private Integer totalDifference;

    /**
     * Số lượng lệch tăng (chỉ số dương)
     */
    @Schema(description = "Số lượng tăng thêm (chỉ tính số dương)", example = "15")
    private Integer increaseQuantity;

    /**
     * Số lượng lệch giảm (chỉ số âm, hiển thị dương)
     */
    @Schema(description = "Số lượng giảm đi (chỉ tính số âm, hiển thị dương)", example = "5")
    private Integer decreaseQuantity;

    /**
     * Ghi chú về thay đổi
     */
    @Schema(description = "Ghi chú về lý do thay đổi", example = "Nhập hàng từ nhà cung cấp ABC")
    private String note;

    // Thông tin sản phẩm để hỗ trợ tìm kiếm
    /**
     * ID biến thể sản phẩm
     */
    @Schema(description = "ID biến thể sản phẩm", example = "1")
    private Long variantId;

    /**
     * Mã biến thể sản phẩm
     */
    @Schema(description = "Mã biến thể sản phẩm", example = "SP001-RED-L")
    private String variantCode;

    /**
     * Tên biến thể sản phẩm
     */
    @Schema(description = "Tên biến thể sản phẩm", example = "Áo Thun Polo - Đỏ - L")
    private String variantName;

    /**
     * ID kho hàng
     */
    @Schema(description = "ID kho hàng", example = "1")
    private Integer warehouseId;

    /**
     * Tên kho hàng
     */
    @Schema(description = "Tên kho hàng", example = "Kho Chính")
    private String warehouseName;

    /**
     * ID giao dịch gốc (để tham chiếu)
     */
    @Schema(description = "ID giao dịch gốc", example = "12345")
    private Long transactionId;

    /**
     * Mã tham chiếu (mã đơn hàng, phiếu nhập...)
     */
    @Schema(description = "Mã tham chiếu đơn hàng, phiếu nhập...", example = "PO-2024-001")
    private String referenceId;
}
