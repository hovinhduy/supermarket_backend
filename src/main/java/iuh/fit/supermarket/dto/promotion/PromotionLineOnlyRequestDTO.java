package iuh.fit.supermarket.dto.promotion;

import iuh.fit.supermarket.enums.PromotionStatus;
import iuh.fit.supermarket.enums.PromotionType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * DTO cho request tạo mới promotion line (không bao gồm detail)
 * Detail phải được tạo riêng thông qua endpoint /lines/{lineId}/details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionLineOnlyRequestDTO {

    /**
     * Tên của line khuyến mãi
     * Bắt buộc, độ dài từ 3-200 ký tự
     */
    @NotBlank(message = "Tên line khuyến mãi không được để trống")
    @Size(min = 3, max = 200, message = "Tên line phải có độ dài từ 3-200 ký tự")
    private String lineName;

    /**
     * Loại khuyến mãi
     * Bắt buộc phải là một trong các loại: BUY_X_GET_Y, ORDER_DISCOUNT,
     * PRODUCT_DISCOUNT
     */
    @NotNull(message = "Loại khuyến mãi không được để trống")
    private PromotionType promotionType;

    /**
     * Mô tả ngắn về line khuyến mãi
     * Tối đa 500 ký tự
     */
    @Size(max = 500, message = "Mô tả line khuyến mãi không được vượt quá 500 ký tự")
    private String description;

    /**
     * Ngày bắt đầu cho line này
     * Bắt buộc
     */
    @NotNull(message = "Ngày bắt đầu line không được để trống")
    private LocalDate startDate;

    /**
     * Ngày kết thúc cho line này
     * Bắt buộc và phải sau ngày bắt đầu
     */
    @NotNull(message = "Ngày kết thúc line không được để trống")
    private LocalDate endDate;

    /**
     * Trạng thái khuyến mãi line
     * Mặc định là UPCOMING khi tạo mới
     */
    @NotNull(message = "Trạng thái line không được để trống")
    private PromotionStatus status;

    /**
     * Validation tùy chỉnh để kiểm tra ngày kết thúc phải sau ngày bắt đầu
     */
    @AssertTrue(message = "Ngày kết thúc line phải sau ngày bắt đầu")
    @JsonIgnore
    public boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true; // Để validation @NotNull xử lý
        }
        return endDate.isAfter(startDate);
    }

    /**
     * Validation để đảm bảo ngày kết thúc phải lớn hơn ngày hiện tại
     */
    @AssertTrue(message = "Ngày kết thúc line phải lớn hơn ngày hiện tại")
    @JsonIgnore
    public boolean isEndDateAfterToday() {
        if (endDate == null) {
            return true; // Để validation @NotNull xử lý
        }
        return endDate.isAfter(LocalDate.now());
    }

    /**
     * Validation để đảm bảo trạng thái chỉ được phép là ACTIVE hoặc PAUSED
     */
    @AssertTrue(message = "Trạng thái line chỉ được phép là ACTIVE hoặc PAUSED")
    @JsonIgnore
    public boolean isStatusValid() {
        if (status == null) {
            return true; // Để validation @NotNull xử lý
        }
        return status == iuh.fit.supermarket.enums.PromotionStatus.ACTIVE ||
               status == iuh.fit.supermarket.enums.PromotionStatus.PAUSED;
    }
}
