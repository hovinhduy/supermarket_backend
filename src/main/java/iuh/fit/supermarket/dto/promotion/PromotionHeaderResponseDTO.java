package iuh.fit.supermarket.dto.promotion;



import iuh.fit.supermarket.enums.PromotionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho response thông tin chương trình khuyến mãi
 * Chứa đầy đủ thông tin header và danh sách các promotion lines
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionHeaderResponseDTO {

    /**
     * ID duy nhất của chương trình khuyến mãi
     */
    private Long promotionId;

    /**
     * Tên chương trình khuyến mãi
     */
    private String promotionName;

    /**
     * Mô tả chương trình khuyến mãi
     */
    private String description;

    /**
     * Ngày bắt đầu chương trình
     */
    
    private LocalDateTime startDate;

    /**
     * Ngày kết thúc chương trình
     */
    
    private LocalDateTime endDate;

    /**
     * Trạng thái chương trình khuyến mãi
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
     * Danh sách các promotion lines thuộc chương trình này
     */
    private List<PromotionLineResponseDTO> promotionLines;

    /**
     * Tổng số promotion lines trong chương trình
     */
    private Integer totalLines;

    /**
     * Số lượng lines đang hoạt động
     */
    private Integer activeLines;

    /**
     * Trạng thái tổng quan của chương trình (computed field)
     */
    private String overallStatus;

    /**
     * Constructor để tạo response từ entity (không bao gồm promotion lines)
     */
    public PromotionHeaderResponseDTO(Long promotionId, String promotionName, String description,
            LocalDateTime startDate, LocalDateTime endDate, PromotionStatus status,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.promotionId = promotionId;
        this.promotionName = promotionName;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Tính toán trạng thái tổng quan dựa trên thời gian hiện tại và trạng thái
     */
    public void calculateOverallStatus() {
        LocalDateTime now = LocalDateTime.now();

        if (status == PromotionStatus.CANCELLED) {
            this.overallStatus = "Đã hủy";
        } else if (now.isBefore(startDate)) {
            this.overallStatus = "Sắp diễn ra";
        } else if (now.isAfter(endDate)) {
            this.overallStatus = "Đã kết thúc";
        } else if (status == PromotionStatus.ACTIVE) {
            this.overallStatus = "Đang hoạt động";
        } else if (status == PromotionStatus.PAUSED) {
            this.overallStatus = "Tạm dừng";
        } else {
            this.overallStatus = "Không xác định";
        }
    }

    /**
     * Tính toán thống kê về số lượng lines
     */
    public void calculateLineStatistics() {
        if (promotionLines != null) {
            this.totalLines = promotionLines.size();
            this.activeLines = (int) promotionLines.stream()
                    .filter(line -> line.getStatus() == PromotionStatus.ACTIVE)
                    .count();
        } else {
            this.totalLines = 0;
            this.activeLines = 0;
        }
    }
}
