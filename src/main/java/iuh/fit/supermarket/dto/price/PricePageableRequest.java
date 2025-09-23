package iuh.fit.supermarket.dto.price;

import iuh.fit.supermarket.enums.PriceType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * DTO cho yêu cầu phân trang và lọc bảng giá
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PricePageableRequest {

    /**
     * Số trang (bắt đầu từ 0)
     */
    @Min(value = 0, message = "Số trang phải >= 0")
    private Integer page = 0;

    /**
     * Số lượng bản ghi trên mỗi trang
     */
    @Min(value = 1, message = "Số lượng bản ghi phải >= 1")
    @Max(value = 100, message = "Số lượng bản ghi không được vượt quá 100")
    private Integer limit = 20;

    /**
     * Từ khóa tìm kiếm (tìm trong tên và mã bảng giá)
     */
    private String searchTerm;

    /**
     * Lọc theo trạng thái bảng giá
     */
    private PriceType status;

    /**
     * Lọc theo ngày bắt đầu từ
     */
    private LocalDateTime startDateFrom;

    /**
     * Lọc theo ngày bắt đầu đến
     */
    private LocalDateTime startDateTo;

    /**
     * Lọc theo ngày kết thúc từ
     */
    private LocalDateTime endDateFrom;

    /**
     * Lọc theo ngày kết thúc đến
     */
    private LocalDateTime endDateTo;

    /**
     * Sắp xếp theo trường nào
     * Các giá trị hợp lệ: createdAt, startDate, endDate, priceName, priceCode
     */
    private String sortBy = "createdAt";

    /**
     * Hướng sắp xếp (asc/desc)
     */
    private String sortDirection = "desc";

    /**
     * Có bao gồm chi tiết giá không
     */
    private Boolean includeDetails = false;

    /**
     * Lọc theo người tạo
     */
    private Integer createdBy;

    /**
     * Lọc theo thời gian tạo từ
     */
    private LocalDateTime createdFrom;

    /**
     * Lọc theo thời gian tạo đến
     */
    private LocalDateTime createdTo;

    /**
     * Kiểm tra hướng sắp xếp có hợp lệ không
     */
    @JsonIgnore
    public boolean isValidSortDirection() {
        return "asc".equalsIgnoreCase(sortDirection) || "desc".equalsIgnoreCase(sortDirection);
    }

    /**
     * Kiểm tra trường sắp xếp có hợp lệ không
     */
    @JsonIgnore
    public boolean isValidSortBy() {
        return sortBy != null && ("createdAt".equals(sortBy) ||
                "startDate".equals(sortBy) ||
                "endDate".equals(sortBy) ||
                "priceName".equals(sortBy) ||
                "priceCode".equals(sortBy) ||
                "updatedAt".equals(sortBy));
    }
}
