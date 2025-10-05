package iuh.fit.supermarket.dto.promotion;

import iuh.fit.supermarket.enums.PromotionStatus;
import iuh.fit.supermarket.enums.PromotionType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
     * Bắt buộc và phải là thời điểm trong tương lai
     */
    @NotNull(message = "Ngày bắt đầu line không được để trống")
    @Future(message = "Ngày bắt đầu line phải là thời điểm trong tương lai")

    private LocalDateTime startDate;

    /**
     * Ngày kết thúc cho line này
     * Bắt buộc và phải sau ngày bắt đầu
     */
    @NotNull(message = "Ngày kết thúc line không được để trống")
    @Future(message = "Ngày kết thúc line phải là thời điểm trong tương lai")

    private LocalDateTime endDate;

    /**
     * Trạng thái khuyến mãi line
     * Mặc định là UPCOMING khi tạo mới
     */
    @NotNull(message = "Trạng thái line không được để trống")
    private PromotionStatus status;

    /**
     * Giới hạn tổng số lần sử dụng
     * Phải là số dương nếu được thiết lập
     */
    @Min(value = 1, message = "Giới hạn tổng số lần sử dụng phải là số dương")
    private Integer maxUsageTotal;

    /**
     * Giới hạn số lần sử dụng mỗi khách hàng
     * Phải là số dương nếu được thiết lập
     */
    @Min(value = 1, message = "Giới hạn số lần sử dụng mỗi khách hàng phải là số dương")
    private Integer maxUsagePerCustomer;

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
     * Validation để đảm bảo maxUsagePerCustomer không vượt quá maxUsageTotal
     */
    @AssertTrue(message = "Giới hạn sử dụng mỗi khách hàng không được vượt quá tổng giới hạn")
    @JsonIgnore
    public boolean isMaxUsagePerCustomerValid() {
        if (maxUsageTotal == null || maxUsagePerCustomer == null) {
            return true; // Không bắt buộc phải có cả hai
        }
        return maxUsagePerCustomer <= maxUsageTotal;
    }
}
