package iuh.fit.supermarket.dto.price;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO cho yêu cầu tạo bảng giá mới
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceCreateRequest {

    /**
     * Tên bảng giá
     */
    @NotBlank(message = "Tên bảng giá không được để trống")
    @Size(max = 255, message = "Tên bảng giá không được vượt quá 255 ký tự")
    private String priceName;

    /**
     * Mã bảng giá (tùy chọn - sẽ tự động tạo nếu không cung cấp)
     * Format tự động: BG + 6 chữ số (VD: BG000001, BG000002, ...)
     */
    @Size(max = 10, message = "Mã bảng giá không được vượt quá 100 ký tự")
    private String priceCode;

    /**
     * Ngày bắt đầu hiệu lực
     */
    @NotNull(message = "Ngày bắt đầu không được để trống")
    @Future(message = "Ngày bắt đầu phải lớn hơn thời gian hiện tại")
    private LocalDateTime startDate;

    /**
     * Ngày kết thúc hiệu lực (có thể null)
     */
    private LocalDateTime endDate;

    /**
     * Mô tả bảng giá
     */
    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    /**
     * Danh sách chi tiết giá (có thể null hoặc empty khi tạo bảng giá trống)
     */
    @Valid
    private List<PriceDetailCreateRequest> priceDetails;

    /**
     * DTO cho yêu cầu tạo chi tiết giá
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceDetailCreateRequest {

        /**
         * ID đơn vị sản phẩm
         */
        @NotNull(message = "ID đơn vị sản phẩm không được để trống")
        private Long productUnitId;

        /**
         * Giá bán
         */
        @NotNull(message = "Giá bán không được để trống")
        @DecimalMin(value = "0.01", message = "Giá bán phải lớn hơn 0")
        @Digits(integer = 10, fraction = 2, message = "Giá bán không hợp lệ")
        private BigDecimal salePrice;
    }

    // /**
    // * Validation tùy chỉnh cho ngày kết thúc
    // */
    // @AssertTrue(message = "Ngày kết thúc phải lớn hơn ngày bắt đầu ít nhất 1
    // ngày")
    // @Schema(hidden = true)
    // public boolean isEndDateValid() {
    // if (endDate == null) {
    // return true; // Cho phép null
    // }
    // return startDate != null && endDate.isAfter(startDate.plusDays(1));
    // }

    // /**
    // * Validation tùy chỉnh cho ngày bắt đầu
    // */
    // @AssertTrue(message = "Ngày bắt đầu phải lớn hơn thời gian hiện tại ít nhất 1
    // phút")
    // @Schema(hidden = true)
    // public boolean isStartDateValid() {
    // if (startDate == null) {
    // return false;
    // }
    // return startDate.isAfter(LocalDateTime.now().plusMinutes(1));
    // }
}
