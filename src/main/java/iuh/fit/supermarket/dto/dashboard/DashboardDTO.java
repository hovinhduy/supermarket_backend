package iuh.fit.supermarket.dto.dashboard;

import iuh.fit.supermarket.enums.TimePeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO cho dashboard metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {
    
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
     * Số lượng khách hàng mới
     */
    private Long newCustomersCount;
    
    /**
     * Số lượng hóa đơn đã xuất (đã thanh toán)
     */
    private Long invoicesCount;
    
    /**
     * Tổng giá trị hóa đơn đã xuất
     */
    private BigDecimal invoicesTotalAmount;
    
    /**
     * Số lượng đơn hàng
     */
    private Long ordersCount;
    
    /**
     * Tổng giá trị đơn hàng
     */
    private BigDecimal ordersTotalAmount;
    
    /**
     * Tổng doanh thu (từ hóa đơn đã thanh toán)
     */
    private BigDecimal totalRevenue;
    
    /**
     * Số lượng đơn trả hàng
     */
    private Long returnsCount;
}
