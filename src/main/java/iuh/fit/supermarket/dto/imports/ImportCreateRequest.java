package iuh.fit.supermarket.dto.imports;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho yêu cầu tạo phiếu nhập hàng mới
 * Bao gồm thông tin phiếu nhập và danh sách chi tiết sản phẩm nhập
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo phiếu nhập hàng mới")
public class ImportCreateRequest {

    /**
     * Mã phiếu nhập (tùy chọn, nếu không truyền sẽ tự động tạo)
     * Format: PN + 6 chữ số (ví dụ: PN000001)
     */
    @Schema(description = "Mã phiếu nhập (tùy chọn, nếu không truyền sẽ tự động tạo)", example = "PN000001")
    private String importCode;

    /**
     * ID nhà cung cấp
     */
    @NotNull(message = "ID nhà cung cấp không được để trống")
    @Positive(message = "ID nhà cung cấp phải là số dương")
    @Schema(description = "ID nhà cung cấp", example = "1", required = true)
    private Integer supplierId;

    /**
     * Ngày nhập hàng (tùy chọn, mặc định là thời gian hiện tại)
     */
    @Schema(description = "Ngày nhập hàng", example = "2024-01-15T10:30:00")
    private LocalDateTime importDate;

    /**
     * Ghi chú cho phiếu nhập
     */
    @Schema(description = "Ghi chú cho phiếu nhập", example = "Nhập hàng đợt 1 tháng 1")
    private String notes;

    /**
     * Danh sách chi tiết sản phẩm nhập hàng
     */
    @NotEmpty(message = "Danh sách sản phẩm nhập không được để trống")
    @Valid
    @Schema(description = "Danh sách chi tiết sản phẩm nhập hàng", required = true)
    private List<ImportDetailRequest> importDetails;

    /**
     * DTO cho chi tiết sản phẩm nhập hàng
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Chi tiết sản phẩm nhập hàng")
    public static class ImportDetailRequest {

        /**
         * ID biến thể sản phẩm
         */
        @NotNull(message = "ID biến thể sản phẩm không được để trống")
        @Positive(message = "ID biến thể sản phẩm phải là số dương")
        @Schema(description = "ID biến thể sản phẩm", example = "1", required = true)
        private Long variantId;

        /**
         * Số lượng nhập
         */
        @NotNull(message = "Số lượng nhập không được để trống")
        @Positive(message = "Số lượng nhập phải là số dương")
        @Schema(description = "Số lượng nhập", example = "100", required = true)
        private Integer quantity;

        /**
         * Ghi chú cho sản phẩm này (tùy chọn)
         */
        @Schema(description = "Ghi chú cho sản phẩm này", example = "Hàng mới về")
        private String notes;
    }
}
