package iuh.fit.supermarket.dto.promotion;

import com.fasterxml.jackson.annotation.JsonIgnore;

import iuh.fit.supermarket.enums.PromotionStatus;
import iuh.fit.supermarket.enums.PromotionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO cho request tạo mới hoặc cập nhật promotion line
 * Chứa thông tin cơ bản của line và chi tiết khuyến mãi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionLineRequestDTO {

    /**
     * Mã chương trình khuyến mãi (unique)
     * Bắt buộc, độ dài từ 3-50 ký tự, chỉ chứa chữ cái, số và dấu gạch dưới
     */
    @NotBlank(message = "Mã chương trình khuyến mãi không được để trống")
    @Size(min = 3, max = 50, message = "Mã chương trình phải có độ dài từ 3-50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "Mã chương trình chỉ được chứa chữ cái, số và dấu gạch dưới")
    private String promotionCode;

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
     * Chi tiết khuyến mãi cho line này
     * Bắt buộc và phải hợp lệ theo loại khuyến mãi
     */
    @NotNull(message = "Chi tiết khuyến mãi không được để trống")
    @Valid
    private PromotionDetailRequestDTO detail;

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
}
