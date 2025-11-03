package iuh.fit.supermarket.dto.promotion;

import iuh.fit.supermarket.enums.PromotionStatus;
import iuh.fit.supermarket.enums.PromotionType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * DTO cho tìm kiếm và lọc chương trình khuyến mãi
 * Hỗ trợ các tiêu chí tìm kiếm phổ biến và phân trang
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionSearchDTO {

    /**
     * Từ khóa tìm kiếm (tên chương trình hoặc mô tả)
     */
    private String keyword;

    /**
     * Trạng thái chương trình khuyến mãi
     */
    private PromotionStatus status;

    /**
     * Loại khuyến mãi
     */
    private PromotionType promotionType;

    /**
     * Ngày bắt đầu tìm kiếm (từ ngày này)
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDateFrom;

    /**
     * Ngày bắt đầu tìm kiếm (đến ngày này)
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDateTo;

    /**
     * Ngày kết thúc tìm kiếm (từ ngày này)
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDateFrom;

    /**
     * Ngày kết thúc tìm kiếm (đến ngày này)
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDateTo;

    /**
     * Chỉ lấy các chương trình đang hoạt động
     */
    private Boolean activeOnly;

    /**
     * Chỉ lấy các chương trình sắp diễn ra
     */
    private Boolean upcomingOnly;

    /**
     * Chỉ lấy các chương trình đã hết hạn
     */
    private Boolean expiredOnly;

    /**
     * Số trang (bắt đầu từ 0)
     */
    private Integer page = 0;

    /**
     * Kích thước trang
     */
    private Integer size = 20;

    /**
     * Trường sắp xếp
     */
    private String sortBy = "createdAt";

    /**
     * Hướng sắp xếp (asc/desc)
     */
    private String sortDirection = "desc";

    /**
     * Kiểm tra xem có điều kiện tìm kiếm nào được áp dụng không
     */
    public boolean hasSearchCriteria() {
        return keyword != null && !keyword.trim().isEmpty() ||
               status != null ||
               promotionType != null ||
               startDateFrom != null ||
               startDateTo != null ||
               endDateFrom != null ||
               endDateTo != null ||
               Boolean.TRUE.equals(activeOnly) ||
               Boolean.TRUE.equals(upcomingOnly) ||
               Boolean.TRUE.equals(expiredOnly);
    }

    /**
     * Validate các tham số phân trang
     */
    public void validatePagination() {
        if (page == null || page < 0) {
            page = 0;
        }
        if (size == null || size <= 0 || size > 100) {
            size = 20;
        }
    }

    /**
     * Validate và chuẩn hóa tham số sắp xếp
     */
    public void validateSorting() {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            sortBy = "createdAt";
        }
        
        // Danh sách các trường được phép sắp xếp
        String[] allowedSortFields = {
            "promotionId", "promotionName", "startDate", "endDate", 
            "status", "createdAt", "updatedAt"
        };
        
        boolean isValidSortField = false;
        for (String field : allowedSortFields) {
            if (field.equals(sortBy)) {
                isValidSortField = true;
                break;
            }
        }
        
        if (!isValidSortField) {
            sortBy = "createdAt";
        }
        
        if (sortDirection == null || 
            (!sortDirection.equalsIgnoreCase("asc") && !sortDirection.equalsIgnoreCase("desc"))) {
            sortDirection = "desc";
        }
    }

    /**
     * Thực hiện tất cả các validation
     */
    public void validate() {
        validatePagination();
        validateSorting();
    }
}
