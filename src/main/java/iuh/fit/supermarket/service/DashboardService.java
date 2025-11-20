package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.dashboard.DashboardDTO;
import iuh.fit.supermarket.dto.dashboard.RevenueChartDTO;
import iuh.fit.supermarket.dto.dashboard.TopProductsResponseDTO;
import iuh.fit.supermarket.enums.TimePeriod;
import iuh.fit.supermarket.enums.TopProductSortBy;

/**
 * Service interface cho Dashboard
 */
public interface DashboardService {
    
    /**
     * Lấy các chỉ số dashboard theo khoảng thời gian
     * 
     * @param period khoảng thời gian cần xem
     * @return DashboardDTO chứa các metrics
     */
    DashboardDTO getDashboardMetrics(TimePeriod period);
    
    /**
     * Lấy dữ liệu biểu đồ doanh thu chi tiết theo khoảng thời gian
     * - Nếu period là TODAY hoặc YESTERDAY: trả về doanh thu theo giờ (0-23)
     * - Nếu period là THIS_WEEK: trả về doanh thu theo ngày trong tuần (Thứ 2 - CN)
     * - Nếu period là THIS_MONTH: trả về doanh thu theo ngày trong tháng
     * 
     * @param period khoảng thời gian cần xem
     * @return RevenueChartDTO chứa dữ liệu biểu đồ
     */
    RevenueChartDTO getRevenueChart(TimePeriod period);
    
    /**
     * Lấy danh sách Top 5 sản phẩm bán chạy nhất
     * 
     * @param period khoảng thời gian cần xem
     * @param sortBy tiêu chí sắp xếp (REVENUE: theo doanh thu, QUANTITY: theo số lượng)
     * @return TopProductsResponseDTO chứa danh sách top 5 sản phẩm
     */
    TopProductsResponseDTO getTopProducts(TimePeriod period, TopProductSortBy sortBy);
}
