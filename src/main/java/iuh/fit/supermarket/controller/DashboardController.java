package iuh.fit.supermarket.controller;

import iuh.fit.supermarket.dto.dashboard.DashboardDTO;
import iuh.fit.supermarket.dto.dashboard.RevenueChartDTO;
import iuh.fit.supermarket.dto.dashboard.TopProductsResponseDTO;
import iuh.fit.supermarket.enums.TimePeriod;
import iuh.fit.supermarket.enums.TopProductSortBy;
import iuh.fit.supermarket.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller cho Dashboard API
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Lấy các chỉ số dashboard theo khoảng thời gian
     * 
     * @param period khoảng thời gian (TODAY, YESTERDAY, THIS_WEEK, THIS_MONTH, THIS_YEAR)
     * @return DashboardDTO
     */
    @GetMapping("/metrics")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<DashboardDTO> getDashboardMetrics(
            @RequestParam(value = "period", defaultValue = "TODAY") TimePeriod period) {
        DashboardDTO metrics = dashboardService.getDashboardMetrics(period);
        return ResponseEntity.ok(metrics);
    }

    /**
     * Lấy dữ liệu biểu đồ doanh thu chi tiết theo khoảng thời gian
     * - Nếu period là TODAY hoặc YESTERDAY: trả về doanh thu theo giờ (0-23)
     * - Nếu period là THIS_WEEK: trả về doanh thu theo ngày trong tuần (Thứ 2 - CN)
     * - Nếu period là THIS_MONTH: trả về doanh thu theo ngày trong tháng (từ ngày 1 đến ngày hiện tại)
     * - Nếu period là THIS_YEAR: trả về doanh thu theo tháng trong năm (Tháng 1-12)
     * 
     * @param period khoảng thời gian (TODAY, YESTERDAY, THIS_WEEK, THIS_MONTH, THIS_YEAR)
     * @return RevenueChartDTO
     */
    @GetMapping("/revenue-chart")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<RevenueChartDTO> getRevenueChart(
            @RequestParam(value = "period", defaultValue = "TODAY") TimePeriod period) {
        RevenueChartDTO chartData = dashboardService.getRevenueChart(period);
        return ResponseEntity.ok(chartData);
    }

    /**
     * Lấy danh sách Top 5 sản phẩm bán chạy nhất
     * 
     * @param period khoảng thời gian (TODAY, YESTERDAY, THIS_WEEK, THIS_MONTH, THIS_YEAR)
     * @param sortBy tiêu chí sắp xếp (REVENUE: theo doanh thu, QUANTITY: theo số lượng)
     * @return TopProductsResponseDTO
     */
    @GetMapping("/top-products")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<TopProductsResponseDTO> getTopProducts(
            @RequestParam(value = "period", defaultValue = "THIS_MONTH") TimePeriod period,
            @RequestParam(value = "sortBy", defaultValue = "REVENUE") TopProductSortBy sortBy) {
        TopProductsResponseDTO topProducts = dashboardService.getTopProducts(period, sortBy);
        return ResponseEntity.ok(topProducts);
    }
}
