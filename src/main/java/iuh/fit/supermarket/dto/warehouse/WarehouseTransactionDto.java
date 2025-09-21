package iuh.fit.supermarket.dto.warehouse;

import io.swagger.v3.oas.annotations.media.Schema;
import iuh.fit.supermarket.entity.WarehouseTransaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho thông tin giao dịch kho
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin giao dịch kho")
public class WarehouseTransactionDto {

    /**
     * ID giao dịch kho
     */
    @Schema(description = "ID giao dịch kho", example = "1")
    private Long transactionId;

    /**
     * Số lượng tồn kho trước khi thực hiện giao dịch
     */
    @Schema(description = "Số lượng tồn kho trước giao dịch", example = "150")
    private Integer beforeQuantity;

    /**
     * Số lượng thay đổi
     * Số dương cho nhập hàng/trả hàng, số âm cho bán hàng/xuất hủy
     */
    @Schema(description = "Số lượng thay đổi", example = "100")
    private Integer quantityChange;

    /**
     * Số lượng tồn kho sau khi thực hiện giao dịch
     * Công thức: newQuantity = beforeQuantity + quantityChange
     */
    @Schema(description = "Số lượng tồn kho sau giao dịch", example = "250")
    private Integer newQuantity;

    /**
     * Loại giao dịch
     */
    @Schema(description = "Loại giao dịch", example = "STOCK_IN")
    private WarehouseTransaction.TransactionType transactionType;

    /**
     * Mã tham chiếu (mã phiếu nhập, mã đơn hàng...)
     */
    @Schema(description = "Mã tham chiếu", example = "IMP20240115001")
    private String referenceId;

    /**
     * Ghi chú
     */
    @Schema(description = "Ghi chú", example = "Nhập hàng từ nhà cung cấp ABC")
    private String notes;

    /**
     * Thời gian giao dịch
     */
    @Schema(description = "Thời gian giao dịch", example = "2024-01-15T10:30:00")
    private LocalDateTime transactionDate;

    /**
     * Thông tin biến thể sản phẩm
     */
    @Schema(description = "Thông tin biến thể sản phẩm")
    private ProductVariantInfo variant;

    /**
     * DTO đơn giản cho thông tin biến thể sản phẩm
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thông tin biến thể sản phẩm")
    public static class ProductVariantInfo {

        /**
         * ID biến thể
         */
        @Schema(description = "ID biến thể", example = "1")
        private Long variantId;

        /**
         * Tên biến thể
         */
        @Schema(description = "Tên biến thể", example = "Áo Thun Polo - Đỏ - L - Cái")
        private String variantName;

        /**
         * Mã biến thể (SKU)
         */
        @Schema(description = "Mã biến thể (SKU)", example = "SKU001")
        private String variantCode;

        /**
         * Mã vạch
         */
        @Schema(description = "Mã vạch", example = "1234567890123")
        private String barcode;

        /**
         * Tên sản phẩm
         */
        @Schema(description = "Tên sản phẩm", example = "Áo Thun Polo")
        private String productName;

        /**
         * Đơn vị
         */
        @Schema(description = "Đơn vị", example = "Cái")
        private String unit;
    }
}
