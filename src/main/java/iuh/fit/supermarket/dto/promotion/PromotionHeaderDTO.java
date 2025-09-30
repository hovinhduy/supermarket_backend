package iuh.fit.supermarket.dto.promotion;

import iuh.fit.supermarket.enums.PromotionStatus;
import iuh.fit.supermarket.enums.PromotionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO đại diện cho thông tin chương trình khuyến mãi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionHeaderDTO {

    /**
     * ID duy nhất của chương trình khuyến mãi
     */
    private Long promotionId;

    /**
     * Mã chương trình khuyến mãi
     */
    private String promotionCode;

    /**
     * Tên chương trình khuyến mãi
     */
    private String promotionName;

    /**
     * Loại khuyến mãi
     */
    private PromotionType promotionType;

    /**
     * Mô tả chương trình
     */
    private String description;

    /**
     * Ngày bắt đầu
     */
    private LocalDateTime startDate;

    /**
     * Ngày kết thúc
     */
    private LocalDateTime endDate;

    /**
     * Trạng thái khuyến mãi
     */
    private PromotionStatus status;

    /**
     * Giới hạn số lần sử dụng mỗi khách hàng
     */
    private Integer maxUsagePerCustomer;

    /**
     * Giới hạn tổng số lần sử dụng
     */
    private Integer maxUsageTotal;

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
     * Danh sách chi tiết khuyến mãi
     */
    private List<PromotionDetailDTO> promotionDetails;
}

