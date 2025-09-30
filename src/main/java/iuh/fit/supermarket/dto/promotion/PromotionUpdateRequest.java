package iuh.fit.supermarket.dto.promotion;

import iuh.fit.supermarket.enums.PromotionStatus;
import iuh.fit.supermarket.enums.PromotionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho yêu cầu cập nhật chương trình khuyến mãi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromotionUpdateRequest {

    /**
     * Tên chương trình khuyến mãi
     */
    @Size(max = 200, message = "Tên khuyến mãi không được vượt quá 200 ký tự")
    private String promotionName;

    /**
     * Loại khuyến mãi
     */
    private PromotionType promotionType;

    /**
     * Mô tả chương trình
     */
    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
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
    @Min(value = 1, message = "Số lần sử dụng mỗi khách hàng phải lớn hơn 0")
    private Integer maxUsagePerCustomer;

    /**
     * Giới hạn tổng số lần sử dụng
     */
    @Min(value = 1, message = "Tổng số lần sử dụng phải lớn hơn 0")
    private Integer maxUsageTotal;

    /**
     * Danh sách chi tiết khuyến mãi
     */
    @Valid
    private List<PromotionDetailCreateRequest> promotionDetails;
}

