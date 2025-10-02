package iuh.fit.supermarket.dto.promotion;

import iuh.fit.supermarket.enums.PromotionStatus;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
     * Bắt buộc và phải là thời điểm trong tương lai
     */
    @NotNull(message = "Ngày bắt đầu không được để trống")
    @Future(message = "Ngày bắt đầu phải là thời điểm trong tương lai")

    private LocalDateTime startDate;

    /**
     * Ngày kết thúc chương trình
     * Bắt buộc và phải sau ngày bắt đầu
     */
    @NotNull(message = "Ngày kết thúc không được để trống")
    @Future(message = "Ngày kết thúc phải là thời điểm trong tương lai")

    private LocalDateTime endDate;

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
     * Validation để đảm bảo khoảng thời gian hợp lý (ít nhất 1 giờ)
     */
    @AssertTrue(message = "Chương trình khuyến mãi phải có thời gian tối thiểu 1 giờ")
    @JsonIgnore
    public boolean isValidDuration() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return startDate.plusHours(1).isBefore(endDate) || startDate.plusHours(1).isEqual(endDate);
    }
}
