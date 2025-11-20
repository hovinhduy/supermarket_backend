package iuh.fit.supermarket.dto.dashboard;

import iuh.fit.supermarket.enums.TimePeriod;
import iuh.fit.supermarket.enums.TopProductSortBy;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO cho danh sách top sản phẩm bán chạy
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopProductsResponseDTO {
    
    /**
     * Khoảng thời gian được filter
     */
    private TimePeriod period;
    
    /**
     * Tiêu chí sắp xếp (REVENUE hoặc QUANTITY)
     */
    private TopProductSortBy sortBy;
    
    /**
     * Từ ngày
     */
    private LocalDate fromDate;
    
    /**
     * Đến ngày
     */
    private LocalDate toDate;
    
    /**
     * Danh sách top 5 sản phẩm bán chạy
     */
    private List<TopProductDTO> topProducts;
}
