package iuh.fit.supermarket.dto.promotion;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import iuh.fit.supermarket.enums.PromotionStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO cho request tạo mới hoặc cập nhật chương trình khuyến mãi
 * Chứa thông tin header và danh sách các promotion lines
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionHeaderRequestDTO {

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
     * Danh sách các promotion lines thuộc chương trình này
     * Bắt buộc phải có ít nhất 1 line
     */
    @NotEmpty(message = "Chương trình khuyến mãi phải có ít nhất một line")
    @Valid
    private List<PromotionLineRequestDTO> promotionLines;

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
}
