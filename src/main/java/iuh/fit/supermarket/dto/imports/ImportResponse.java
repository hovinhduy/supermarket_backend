package iuh.fit.supermarket.dto.imports;

import io.swagger.v3.oas.annotations.media.Schema;
import iuh.fit.supermarket.dto.employee.EmployeeDto;
import iuh.fit.supermarket.dto.supplier.SupplierDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho phản hồi thông tin phiếu nhập hàng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin phiếu nhập hàng")
public class ImportResponse {

    /**
     * ID phiếu nhập
     */
    @Schema(description = "ID phiếu nhập", example = "1")
    private Integer importId;

    /**
     * Mã phiếu nhập
     */
    @Schema(description = "Mã phiếu nhập", example = "IMP20240115001")
    private String importCode;

    /**
     * Ngày nhập hàng
     */
    @Schema(description = "Ngày nhập hàng", example = "2024-01-15T10:30:00")
    private LocalDateTime importDate;

    /**
     * Ghi chú
     */
    @Schema(description = "Ghi chú", example = "Nhập hàng đợt 1 tháng 1")
    private String notes;

    /**
     * Thời gian tạo
     */
    @Schema(description = "Thời gian tạo", example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;

    /**
     * Thông tin nhà cung cấp
     */
    @Schema(description = "Thông tin nhà cung cấp")
    private SupplierDto supplier;

    /**
     * Thông tin nhân viên tạo phiếu
     */
    @Schema(description = "Thông tin nhân viên tạo phiếu")
    private EmployeeDto createdBy;

    /**
     * Danh sách chi tiết nhập hàng
     */
    @Schema(description = "Danh sách chi tiết nhập hàng")
    private List<ImportDetailResponse> importDetails;

    /**
     * Tổng số lượng sản phẩm nhập
     */
    @Schema(description = "Tổng số lượng sản phẩm nhập", example = "500")
    private Integer totalQuantity;

    /**
     * Số lượng loại sản phẩm khác nhau
     */
    @Schema(description = "Số lượng loại sản phẩm khác nhau", example = "5")
    private Integer totalUnits;

    /**
     * DTO cho chi tiết nhập hàng
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Chi tiết nhập hàng")
    public static class ImportDetailResponse {

        /**
         * ID chi tiết nhập hàng
         */
        @Schema(description = "ID chi tiết nhập hàng", example = "1")
        private Integer importDetailId;

        /**
         * Số lượng nhập
         */
        @Schema(description = "Số lượng nhập", example = "100")
        private Integer quantity;

        /**
         * Thời gian tạo
         */
        @Schema(description = "Thời gian tạo", example = "2024-01-15T10:30:00")
        private LocalDateTime createdAt;

        /**
         * Thông tin đơn vị sản phẩm
         */
        @Schema(description = "Thông tin đơn vị sản phẩm")
        private ProductUnitInfo productUnit;

        /**
         * DTO đơn giản cho thông tin đơn vị sản phẩm
         */
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Schema(description = "Thông tin đơn vị sản phẩm")
        public static class ProductUnitInfo {

            /**
             * ID đơn vị sản phẩm
             */
            @Schema(description = "ID đơn vị sản phẩm", example = "1")
            private Long productUnitId;

            /**
             * Mã đơn vị sản phẩm (SKU)
             */
            @Schema(description = "Mã đơn vị sản phẩm (SKU)", example = "SKU001")
            private String code;

            /**
             * Mã vạch
             */
            @Schema(description = "Mã vạch", example = "1234567890123")
            private String barcode;

            /**
             * Tỷ lệ quy đổi
             */
            @Schema(description = "Tỷ lệ quy đổi", example = "1")
            private Integer conversionValue;

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
}
