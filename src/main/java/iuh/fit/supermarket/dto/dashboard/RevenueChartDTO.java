package iuh.fit.supermarket.dto.dashboard;

import iuh.fit.supermarket.enums.TimePeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO cho biểu đồ doanh thu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueChartDTO {
    
    /**
     * Khoảng thời gian được filter
     */
    private TimePeriod period;
    
    /**
     * Từ ngày
     */
    private LocalDate fromDate;
    
    /**
     * Đến ngày
     */
    private LocalDate toDate;
    
    /**
     * Tổng doanh thu trong khoảng thời gian
     */
    private BigDecimal totalRevenue;
    
    /**
     * Tổng số hóa đơn
     */
    private Long totalInvoices;
    
    /**
     * Danh sách chi tiết doanh thu theo thời gian
     * - Nếu filter theo ngày: danh sách 24 điểm dữ liệu (theo giờ 0-23)
     * - Nếu filter theo tuần: danh sách 7 điểm dữ liệu (Thứ 2 đến CN)
     * - Nếu filter theo tháng: danh sách theo ngày trong tháng (từ ngày 1 đến ngày hiện tại)
     */
    private List<RevenueDetailDTO> details;
}
