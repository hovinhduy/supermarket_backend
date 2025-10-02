package iuh.fit.supermarket.dto.promotion;

import iuh.fit.supermarket.enums.PromotionStatus;
import iuh.fit.supermarket.enums.PromotionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho response thông tin promotion line
 * Chứa đầy đủ thông tin line và chi tiết khuyến mãi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionLineResponseDTO {

    /**
     * ID duy nhất của promotion line
     */
    private Long promotionLineId;

    /**
     * Mã chương trình khuyến mãi (unique)
     */
    private String promotionCode;

    /**
     * Loại khuyến mãi
     */
    private PromotionType promotionType;

    /**
     * Mô tả ngắn về line khuyến mãi
     */
    private String description;

    /**
     * Ngày bắt đầu cho line này
     */

    private LocalDateTime startDate;

    /**
     * Ngày kết thúc cho line này
     */

    private LocalDateTime endDate;

    /**
     * Trạng thái khuyến mãi line
     */
    private PromotionStatus status;

    /**
     * Giới hạn tổng số lần sử dụng
     */
    private Integer maxUsageTotal;

    /**
     * Giới hạn số lần sử dụng mỗi khách hàng
     */
    private Integer maxUsagePerCustomer;

    /**
     * Số lần đã sử dụng hiện tại
     */
    private Integer currentUsageCount;

    /**
     * Thời gian tạo
     */

    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật gần nhất
     */

    private LocalDateTime updatedAt;

    /**
     * Chi tiết khuyến mãi cho line này
     */
    private PromotionDetailResponseDTO detail;

    /**
     * Tỷ lệ sử dụng hiện tại (computed field)
     */
    private Double usagePercentage;

    /**
     * Trạng thái hoạt động của line (computed field)
     */
    private String activeStatus;

    /**
     * Số lần sử dụng còn lại (computed field)
     */
    private Integer remainingUsage;

    /**
     * Constructor cơ bản không bao gồm detail
     */
    public PromotionLineResponseDTO(Long promotionLineId, String promotionCode, PromotionType promotionType,
            String description, LocalDateTime startDate, LocalDateTime endDate,
            PromotionStatus status, Integer maxUsageTotal, Integer maxUsagePerCustomer,
            Integer currentUsageCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.promotionLineId = promotionLineId;
        this.promotionCode = promotionCode;
        this.promotionType = promotionType;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.maxUsageTotal = maxUsageTotal;
        this.maxUsagePerCustomer = maxUsagePerCustomer;
        this.currentUsageCount = currentUsageCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Tính toán tỷ lệ sử dụng hiện tại
     */
    public void calculateUsagePercentage() {
        if (maxUsageTotal != null && maxUsageTotal > 0) {
            this.usagePercentage = (double) currentUsageCount / maxUsageTotal * 100;
        } else {
            this.usagePercentage = 0.0;
        }
    }

    /**
     * Tính toán trạng thái hoạt động dựa trên thời gian và usage
     */
    public void calculateActiveStatus() {
        LocalDateTime now = LocalDateTime.now();

        if (status == PromotionStatus.CANCELLED) {
            this.activeStatus = "Đã hủy";
        } else if (now.isBefore(startDate)) {
            this.activeStatus = "Chưa bắt đầu";
        } else if (now.isAfter(endDate)) {
            this.activeStatus = "Đã kết thúc";
        } else if (maxUsageTotal != null && currentUsageCount >= maxUsageTotal) {
            this.activeStatus = "Đã hết lượt sử dụng";
        } else if (status == PromotionStatus.ACTIVE) {
            this.activeStatus = "Đang hoạt động";
        } else if (status == PromotionStatus.PAUSED) {
            this.activeStatus = "Tạm dừng";
        } else {
            this.activeStatus = "Không hoạt động";
        }
    }

    /**
     * Tính toán số lần sử dụng còn lại
     */
    public void calculateRemainingUsage() {
        if (maxUsageTotal != null) {
            this.remainingUsage = Math.max(0, maxUsageTotal - currentUsageCount);
        } else {
            this.remainingUsage = null; // Không giới hạn
        }
    }

    /**
     * Thực hiện tất cả các tính toán computed fields
     */
    public void calculateComputedFields() {
        calculateUsagePercentage();
        calculateActiveStatus();
        calculateRemainingUsage();
    }
}
