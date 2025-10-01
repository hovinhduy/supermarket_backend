package iuh.fit.supermarket.dto.price;

import iuh.fit.supermarket.enums.PriceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho yêu cầu cập nhật bảng giá
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceUpdateRequest {

    /**
     * Tên bảng giá
     */
    @Size(max = 255, message = "Tên bảng giá không được vượt quá 255 ký tự")
    private String priceName;

    /**
     * Ngày bắt đầu hiệu lực (chỉ cho phép cập nhật nếu bảng giá chưa active)
     */
    private LocalDateTime startDate;

    /**
     * Ngày kết thúc hiệu lực
     */
    private LocalDateTime endDate;

    /**
     * Mô tả bảng giá
     */
    @Size(max = 1000, message = "Mô tả không được vượt quá 1000 ký tự")
    private String description;

    /**
     * Trạng thái bảng giá (chỉ cho phép PAUSED/CURRENT cho manual update)
     */
    private PriceType status;

    /**
     * Danh sách chi tiết giá cần cập nhật
     */
    @Valid
    private List<PriceDetailUpdateRequest> priceDetails;

    /**
     * DTO cho yêu cầu cập nhật chi tiết giá
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PriceDetailUpdateRequest {

        /**
         * ID chi tiết giá (null nếu là tạo mới)
         */
        private Long priceDetailId;

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

        /**
         * Có xóa chi tiết này không (dùng cho soft delete)
         */
        private Boolean deleted = false;
    }

    /**
     * Validation tùy chỉnh cho ngày kết thúc
     */
    @AssertTrue(message = "Ngày kết thúc phải lớn hơn ngày bắt đầu ít nhất 1 ngày")
    public boolean isEndDateValid() {
        if (endDate == null || startDate == null) {
            return true;
        }
        return endDate.isAfter(startDate.plusDays(1));
    }
}
