package iuh.fit.supermarket.dto.promotion;

import iuh.fit.supermarket.enums.PromotionStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * DTO cho request tạo mới chỉ promotion header (không bao gồm lines)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionHeaderOnlyRequestDTO {

    /**
     * Tên chương trình khuyến mãi
     * Bắt buộc, độ dài từ 3-200 ký tự
     */
    @NotBlank(message = "Tên chương trình khuyến mãi không được để trống")
    @Size(min = 3, max = 200, message = "Tên chương trình khuyến mãi phải có độ dài từ 3-200 ký tự")
    private String promotionName;

    /**
     * Mô tả chương trình khuyến mãi
     * Tối đa 500 ký tự
     */
    @Size(max = 500, message = "Mô tả chương trình không được vượt quá 500 ký tự")
    private String description;

    /**
     * Ngày bắt đầu chương trình
     * Bắt buộc
     */
    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDate startDate;

    /**
     * Ngày kết thúc chương trình
     * Bắt buộc và phải sau ngày bắt đầu
     */
    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDate endDate;

    /**
     * Trạng thái chương trình khuyến mãi
     * Mặc định là UPCOMING khi tạo mới
     */
    @NotNull(message = "Trạng thái chương trình không được để trống")
    private PromotionStatus status;

    /**
     * Validation tùy chỉnh để kiểm tra ngày kết thúc phải sau ngày bắt đầu
     */
    @AssertTrue(message = "Ngày kết thúc phải sau ngày bắt đầu")
    @JsonIgnore
    public boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true; // Để validation @NotNull xử lý
        }
        return endDate.isAfter(startDate);
    }

    /**
     * Validation để đảm bảo khoảng thời gian hợp lý (ít nhất 1 ngày)
     */
    @AssertTrue(message = "Chương trình khuyến mãi phải có thời gian tối thiểu 1 ngày")
    @JsonIgnore
    public boolean isValidDuration() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return startDate.plusDays(1).isBefore(endDate) || startDate.plusDays(1).isEqual(endDate);
    }

    /**
     * Validation để đảm bảo ngày kết thúc phải lớn hơn ngày hiện tại
     */
    @AssertTrue(message = "Ngày kết thúc phải lớn hơn ngày hiện tại")
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
    @AssertTrue(message = "Trạng thái chỉ được phép là ACTIVE hoặc PAUSED")
    @JsonIgnore
    public boolean isStatusValid() {
        if (status == null) {
            return true; // Để validation @NotNull xử lý
        }
        return status == PromotionStatus.ACTIVE || status == PromotionStatus.PAUSED;
    }
}
