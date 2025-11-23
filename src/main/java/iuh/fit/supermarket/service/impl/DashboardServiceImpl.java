package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.dashboard.DashboardDTO;
import iuh.fit.supermarket.dto.dashboard.RevenueChartDTO;
import iuh.fit.supermarket.dto.dashboard.RevenueDetailDTO;
import iuh.fit.supermarket.dto.dashboard.TopProductDTO;
import iuh.fit.supermarket.dto.dashboard.TopProductsResponseDTO;
import iuh.fit.supermarket.enums.TimePeriod;
import iuh.fit.supermarket.enums.TopProductSortBy;
import iuh.fit.supermarket.repository.CustomerRepository;
import iuh.fit.supermarket.repository.OrderRepository;
import iuh.fit.supermarket.repository.ReturnInvoiceHeaderRepository;
import iuh.fit.supermarket.repository.SaleInvoiceDetailRepository;
import iuh.fit.supermarket.repository.SaleInvoiceHeaderRepository;
import iuh.fit.supermarket.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;

import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation của DashboardService
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final SaleInvoiceHeaderRepository saleInvoiceHeaderRepository;
    private final SaleInvoiceDetailRepository saleInvoiceDetailRepository;
    private final ReturnInvoiceHeaderRepository returnInvoiceHeaderRepository;

    @Override
    public DashboardDTO getDashboardMetrics(TimePeriod period) {
        // 1. Xác định khoảng thời gian
        LocalDate fromDate;
        LocalDate toDate;
        
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        
        switch (period) {
            case TODAY:
                fromDate = today;
                toDate = today;
                break;
            case YESTERDAY:
                fromDate = today.minusDays(1);
                toDate = fromDate;
                break;
            case THIS_WEEK:
                // Tuần bắt đầu từ thứ 2
                fromDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                toDate = today; // Tính đến hiện tại
                break;
            case THIS_MONTH:
                fromDate = today.with(TemporalAdjusters.firstDayOfMonth());
                toDate = today;
                break;
            case THIS_YEAR:
                fromDate = today.with(TemporalAdjusters.firstDayOfYear());
                toDate = today;
                break;
            default:
                fromDate = today;
                toDate = today;
        }
        
        // Convert to LocalDateTime for Customer query (which uses createdAt timestamp)
        LocalDateTime fromDateTime = fromDate.atStartOfDay();
        LocalDateTime toDateTime = toDate.atTime(LocalTime.MAX);
        
        // 2. Query metrics
        
        // Khách hàng mới
        long newCustomersCount = customerRepository.countNewCustomersByDateRange(fromDateTime, toDateTime);
        
        // Hóa đơn đã xuất (đã thanh toán)
        long invoicesCount = saleInvoiceHeaderRepository.countPaidInvoicesByDateRange(fromDate, toDate);
        BigDecimal invoicesTotalAmount = saleInvoiceHeaderRepository.sumPaidInvoicesTotalByDateRange(fromDate, toDate);
        
        // Đơn hàng
        long ordersCount = orderRepository.countOrdersByDateRange(fromDate, toDate);
        BigDecimal ordersTotalAmount = orderRepository.sumOrdersTotalByDateRange(fromDate, toDate);
        
        // Trả hàng (chỉ lấy số lượng đơn trả)
        long returnsCount = returnInvoiceHeaderRepository.countReturnsByDateRange(fromDate, toDate);
        
        // 3. Build response
        return DashboardDTO.builder()
                .period(period)
                .fromDate(fromDate)
                .toDate(toDate)
                .newCustomersCount(newCustomersCount)
                .invoicesCount(invoicesCount)
                .invoicesTotalAmount(invoicesTotalAmount)
                .ordersCount(ordersCount)
                .ordersTotalAmount(ordersTotalAmount)
                .totalRevenue(invoicesTotalAmount) // Doanh thu = tổng tiền hóa đơn đã thanh toán
                .returnsCount(returnsCount)
                .build();
    }

    @Override
    public RevenueChartDTO getRevenueChart(TimePeriod period) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate fromDate;
        LocalDate toDate;
        List<RevenueDetailDTO> details;

        switch (period) {
            case TODAY:
            case YESTERDAY:
                // Lọc theo ngày -> hiển thị theo giờ (0-23)
                LocalDate targetDate = (period == TimePeriod.TODAY) ? today : today.minusDays(1);
                fromDate = targetDate;
                toDate = targetDate;
                details = getRevenueByHour(targetDate);
                break;

            case THIS_WEEK:
                // Lọc theo tuần -> hiển thị theo ngày (Thứ 2-CN)
                fromDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                toDate = today;
                details = getRevenueByWeek(fromDate, toDate);
                break;

            case THIS_MONTH:
                // Lọc theo tháng -> hiển thị theo ngày trong tháng
                fromDate = today.with(TemporalAdjusters.firstDayOfMonth());
                toDate = today;
                details = getRevenueByMonth(fromDate, toDate);
                break;

            case THIS_YEAR:
                // Lọc theo năm -> hiển thị theo tháng (1-12)
                fromDate = today.with(TemporalAdjusters.firstDayOfYear());
                toDate = today;
                details = getRevenueByYear(fromDate, toDate);
                break;

            default:
                // Mặc định là hôm nay
                fromDate = today;
                toDate = today;
                details = getRevenueByHour(today);
                break;
        }

        // Tính tổng doanh thu và số hóa đơn
        BigDecimal totalRevenue = details.stream()
                .map(RevenueDetailDTO::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Long totalInvoices = details.stream()
                .mapToLong(RevenueDetailDTO::getInvoiceCount)
                .sum();

        return RevenueChartDTO.builder()
                .period(period)
                .fromDate(fromDate)
                .toDate(toDate)
                .totalRevenue(totalRevenue)
                .totalInvoices(totalInvoices)
                .details(details)
                .build();
    }

    /**
     * Lấy doanh thu theo giờ trong ngày (0-23)
     */
    private List<RevenueDetailDTO> getRevenueByHour(LocalDate date) {
        List<Object[]> results = saleInvoiceHeaderRepository.getRevenueByHourOfDay(date);
        
        // Tạo map để lưu dữ liệu theo giờ
        Map<Integer, RevenueDetailDTO> revenueMap = new HashMap<>();
        
        for (Object[] row : results) {
            Integer hour = (Integer) row[0];
            BigDecimal revenue = (BigDecimal) row[1];
            Long count = (Long) row[2];
            
            revenueMap.put(hour, RevenueDetailDTO.builder()
                    .label(hour + "h")
                    .revenue(revenue)
                    .invoiceCount(count)
                    .build());
        }
        
        // Tạo danh sách đầy đủ 24 giờ (0-23), điền 0 cho giờ không có dữ liệu
        List<RevenueDetailDTO> hourlyRevenue = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            if (revenueMap.containsKey(hour)) {
                hourlyRevenue.add(revenueMap.get(hour));
            } else {
                hourlyRevenue.add(RevenueDetailDTO.builder()
                        .label(hour + "h")
                        .revenue(BigDecimal.ZERO)
                        .invoiceCount(0L)
                        .build());
            }
        }
        
        return hourlyRevenue;
    }

    /**
     * Lấy doanh thu theo ngày trong tuần (Thứ 2 - CN)
     */
    private List<RevenueDetailDTO> getRevenueByWeek(LocalDate fromDate, LocalDate toDate) {
        List<Object[]> results = saleInvoiceHeaderRepository.getRevenueByDayOfWeek(fromDate, toDate);
        
        // Tạo map để lưu dữ liệu theo ngày
        Map<LocalDate, RevenueDetailDTO> revenueMap = new HashMap<>();
        
        for (Object[] row : results) {
            // Chuyển đổi từ java.sql.Date sang LocalDate
            LocalDate date = (row[0] instanceof Date) 
                    ? ((Date) row[0]).toLocalDate() 
                    : (LocalDate) row[0];
            BigDecimal revenue = (BigDecimal) row[1];
            Long count = (Long) row[2];
            
            String dayLabel = getDayOfWeekLabel(date.getDayOfWeek());
            
            revenueMap.put(date, RevenueDetailDTO.builder()
                    .label(dayLabel)
                    .revenue(revenue)
                    .invoiceCount(count)
                    .build());
        }
        
        // Tạo danh sách đầy đủ 7 ngày trong tuần, điền 0 cho ngày không có dữ liệu
        List<RevenueDetailDTO> weeklyRevenue = new ArrayList<>();
        LocalDate currentDate = fromDate;
        
        while (!currentDate.isAfter(toDate)) {
            if (revenueMap.containsKey(currentDate)) {
                weeklyRevenue.add(revenueMap.get(currentDate));
            } else {
                String dayLabel = getDayOfWeekLabel(currentDate.getDayOfWeek());
                weeklyRevenue.add(RevenueDetailDTO.builder()
                        .label(dayLabel)
                        .revenue(BigDecimal.ZERO)
                        .invoiceCount(0L)
                        .build());
            }
            currentDate = currentDate.plusDays(1);
        }
        
        return weeklyRevenue;
    }

    /**
     * Lấy doanh thu theo ngày trong tháng (từ ngày 1 đến ngày hiện tại)
     */
    private List<RevenueDetailDTO> getRevenueByMonth(LocalDate fromDate, LocalDate toDate) {
        List<Object[]> results = saleInvoiceHeaderRepository.getRevenueByDayOfMonth(fromDate, toDate);
        
        // Tạo map để lưu dữ liệu theo ngày
        Map<LocalDate, RevenueDetailDTO> revenueMap = new HashMap<>();
        
        for (Object[] row : results) {
            // Chuyển đổi từ java.sql.Date sang LocalDate
            LocalDate date = (row[0] instanceof Date) 
                    ? ((Date) row[0]).toLocalDate() 
                    : (LocalDate) row[0];
            BigDecimal revenue = (BigDecimal) row[1];
            Long count = (Long) row[2];
            
            revenueMap.put(date, RevenueDetailDTO.builder()
                    .label("Ngày " + date.getDayOfMonth())
                    .revenue(revenue)
                    .invoiceCount(count)
                    .build());
        }
        
        // Tạo danh sách đầy đủ từ ngày 1 đến ngày hiện tại, điền 0 cho ngày không có dữ liệu
        List<RevenueDetailDTO> monthlyRevenue = new ArrayList<>();
        LocalDate currentDate = fromDate;
        
        while (!currentDate.isAfter(toDate)) {
            if (revenueMap.containsKey(currentDate)) {
                monthlyRevenue.add(revenueMap.get(currentDate));
            } else {
                monthlyRevenue.add(RevenueDetailDTO.builder()
                        .label("Ngày " + currentDate.getDayOfMonth())
                        .revenue(BigDecimal.ZERO)
                        .invoiceCount(0L)
                        .build());
            }
            currentDate = currentDate.plusDays(1);
        }
        
        return monthlyRevenue;
    }

    /**
     * Chuyển đổi DayOfWeek sang nhãn tiếng Việt
     */
    private String getDayOfWeekLabel(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY:
                return "Thứ 2";
            case TUESDAY:
                return "Thứ 3";
            case WEDNESDAY:
                return "Thứ 4";
            case THURSDAY:
                return "Thứ 5";
            case FRIDAY:
                return "Thứ 6";
            case SATURDAY:
                return "Thứ 7";
            case SUNDAY:
                return "Chủ nhật";
            default:
                return "";
        }
    }

    /**
     * Lấy doanh thu theo tháng trong năm (từ tháng 1 đến tháng hiện tại)
     */
    private List<RevenueDetailDTO> getRevenueByYear(LocalDate fromDate, LocalDate toDate) {
        List<Object[]> results = saleInvoiceHeaderRepository.getRevenueByMonthOfYear(fromDate, toDate);
        
        // Tạo map để lưu dữ liệu theo tháng
        Map<Integer, RevenueDetailDTO> revenueMap = new HashMap<>();
        
        for (Object[] row : results) {
            Integer month = (Integer) row[0];
            BigDecimal revenue = (BigDecimal) row[1];
            Long count = (Long) row[2];
            
            revenueMap.put(month, RevenueDetailDTO.builder()
                    .label("Tháng " + month)
                    .revenue(revenue)
                    .invoiceCount(count)
                    .build());
        }
        
        // Tạo danh sách đầy đủ 12 tháng, điền 0 cho tháng không có dữ liệu
        List<RevenueDetailDTO> yearlyRevenue = new ArrayList<>();
        int currentMonth = toDate.getMonthValue();
        
        for (int month = 1; month <= 12; month++) {
            if (revenueMap.containsKey(month)) {
                yearlyRevenue.add(revenueMap.get(month));
            } else {
                // Chỉ thêm tháng nếu <= tháng hiện tại
                if (month <= currentMonth) {
                    yearlyRevenue.add(RevenueDetailDTO.builder()
                            .label("Tháng " + month)
                            .revenue(BigDecimal.ZERO)
                            .invoiceCount(0L)
                            .build());
                }
            }
        }
        
        return yearlyRevenue;
    }

    @Override
    public TopProductsResponseDTO getTopProducts(TimePeriod period, TopProductSortBy sortBy) {
        // 1. Xác định khoảng thời gian
        LocalDate fromDate;
        LocalDate toDate;
        
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        
        switch (period) {
            case TODAY:
                fromDate = today;
                toDate = today;
                break;
            case YESTERDAY:
                fromDate = today.minusDays(1);
                toDate = fromDate;
                break;
            case THIS_WEEK:
                fromDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                toDate = today;
                break;
            case THIS_MONTH:
                fromDate = today.with(TemporalAdjusters.firstDayOfMonth());
                toDate = today;
                break;
            case THIS_YEAR:
                fromDate = today.with(TemporalAdjusters.firstDayOfYear());
                toDate = today;
                break;
            default:
                fromDate = today;
                toDate = today;
        }
        
        // 2. Lấy dữ liệu top 5 sản phẩm
        List<Object[]> results;
        org.springframework.data.domain.PageRequest pageRequest = 
                org.springframework.data.domain.PageRequest.of(0, 5);
        
        if (sortBy == TopProductSortBy.REVENUE) {
            results = saleInvoiceDetailRepository.findTop5ProductsByRevenue(fromDate, toDate, pageRequest);
        } else {
            results = saleInvoiceDetailRepository.findTop5ProductsByQuantity(fromDate, toDate, pageRequest);
        }
        
        // 3. Chuyển đổi kết quả thành DTO
        List<TopProductDTO> topProducts = new ArrayList<>();
        int rank = 1;
        
        for (Object[] row : results) {
            Long productUnitId = (Long) row[0];
            Long productId = (Long) row[1];
            String productName = (String) row[2];
            String unitName = (String) row[3];
            String barcode = (String) row[4];
            Long totalQuantity = (Long) row[5];
            BigDecimal totalRevenue = (BigDecimal) row[6];
            
            topProducts.add(TopProductDTO.builder()
                    .productUnitId(productUnitId)
                    .productId(productId)
                    .productName(productName)
                    .unitName(unitName)
                    .barcode(barcode)
                    .totalQuantitySold(totalQuantity)
                    .totalRevenue(totalRevenue)
                    .rank(rank++)
                    .build());
        }
        
        // 4. Build response
        return TopProductsResponseDTO.builder()
                .period(period)
                .sortBy(sortBy)
                .fromDate(fromDate)
                .toDate(toDate)
                .topProducts(topProducts)
                .build();
    }
}
