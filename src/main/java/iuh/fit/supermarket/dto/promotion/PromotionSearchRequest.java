package iuh.fit.supermarket.dto.promotion;

import iuh.fit.supermarket.enums.PromotionStatus;
import iuh.fit.supermarket.enums.PromotionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho yêu cầu tìm kiếm khuyến mãi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionSearchRequest {

    /**
     * Từ khóa tìm kiếm (tên hoặc mã khuyến mãi)
     */
    private String keyword;

    /**
     * Loại khuyến mãi
     */
    private PromotionType promotionType;

    /**
     * Trạng thái khuyến mãi
     */
    private PromotionStatus status;

    /**
     * Tìm theo ngày bắt đầu từ
     */
    private LocalDateTime startDateFrom;

    /**
     * Tìm theo ngày bắt đầu đến
     */
    private LocalDateTime startDateTo;

    /**
     * Tìm theo ngày kết thúc từ
     */
    private LocalDateTime endDateFrom;

    /**
     * Tìm theo ngày kết thúc đến
     */
    private LocalDateTime endDateTo;

    /**
     * Số trang (bắt đầu từ 0)
     */
    @lombok.Builder.Default
    private Integer page = 0;

    /**
     * Số lượng bản ghi mỗi trang
     */
    @lombok.Builder.Default
    private Integer size = 10;

    /**
     * Trường sắp xếp
     */
    @lombok.Builder.Default
    private String sortBy = "createdAt";

    /**
     * Hướng sắp xếp (ASC hoặc DESC)
     */
    @lombok.Builder.Default
    private String sortDirection = "DESC";
}
