package iuh.fit.supermarket.dto.promotion;

import iuh.fit.supermarket.enums.PromotionStatus;
import iuh.fit.supermarket.enums.PromotionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    private LocalDate startDate;

    /**
     * Ngày kết thúc cho line này
     */
    private LocalDate endDate;

    /**
     * Trạng thái khuyến mãi line
     */
    private PromotionStatus status;

    /**
     * Thời gian tạo
     */

    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật gần nhất
     */

    private LocalDateTime updatedAt;

    /**
     * Các chi tiết khuyến mãi cho line này (một line có thể có nhiều details)
     */
    private List<PromotionDetailResponseDTO> details;

    /**
     * Trạng thái hoạt động của line (computed field)
     */
    private String activeStatus;

    /**
     * Constructor cơ bản không bao gồm detail
     */
    public PromotionLineResponseDTO(Long promotionLineId, String promotionCode, PromotionType promotionType,
            String description, LocalDate startDate, LocalDate endDate,
            PromotionStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.promotionLineId = promotionLineId;
        this.promotionCode = promotionCode;
        this.promotionType = promotionType;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Tính toán trạng thái hoạt động dựa trên ngày
     */
    public void calculateActiveStatus() {
        LocalDate now = LocalDate.now();

        if (status == PromotionStatus.CANCELLED) {
            this.activeStatus = "Đã hủy";
        } else if (now.isBefore(startDate)) {
            this.activeStatus = "Chưa bắt đầu";
        } else if (now.isAfter(endDate)) {
            this.activeStatus = "Đã kết thúc";
        } else if (status == PromotionStatus.ACTIVE) {
            this.activeStatus = "Đang hoạt động";
        } else if (status == PromotionStatus.PAUSED) {
            this.activeStatus = "Tạm dừng";
        } else {
            this.activeStatus = "Không hoạt động";
        }
    }
}
