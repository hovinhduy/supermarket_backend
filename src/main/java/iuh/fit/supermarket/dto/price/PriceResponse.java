package iuh.fit.supermarket.dto.price;

import iuh.fit.supermarket.enums.PriceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho phản hồi thông tin bảng giá
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceResponse {

    /**
     * ID bảng giá
     */
    private Long priceId;

    /**
     * Tên bảng giá
     */
    private String priceName;

    /**
     * Mã bảng giá
     */
    private String priceCode;

    /**
     * Ngày bắt đầu hiệu lực
     */
    private LocalDateTime startDate;

    /**
     * Ngày kết thúc hiệu lực
     */
    private LocalDateTime endDate;

    /**
     * Mô tả bảng giá
     */
    private String description;

    /**
     * Trạng thái bảng giá
     */
    private PriceType status;

    /**
     * Thời gian tạo
     */
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật
     */
    private LocalDateTime updatedAt;

    /**
     * Thông tin người tạo
     */
    private EmployeeInfo createdBy;

    /**
     * Thông tin người cập nhật
     */
    private EmployeeInfo updatedBy;

    /**
     * Số lượng chi tiết giá
     */
    private Integer priceDetailCount;

    /**
     * Danh sách chi tiết giá (có thể null nếu không load)
     */
    private List<PriceDetailDto> priceDetails;

    /**
     * DTO cho thông tin nhân viên
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmployeeInfo {
        private Integer employeeId;
        private String name;
        private String email;
    }

    /**
     * Kiểm tra bảng giá có đang hoạt động không
     */
    public boolean isActive() {
        return status == PriceType.ACTIVE;
    }

    /**
     * Kiểm tra bảng giá có thể chỉnh sửa không
     */
    public boolean isEditable() {
        return status == PriceType.ACTIVE || status == PriceType.PAUSED;
    }

    /**
     * Kiểm tra bảng giá có hết hạn không
     */
    public boolean isExpired() {
        return status == PriceType.EXPIRED || 
               (endDate != null && endDate.isBefore(LocalDateTime.now()));
    }
}
