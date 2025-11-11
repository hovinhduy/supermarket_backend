package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.report.CustomerSalesReportRequestDTO;
import iuh.fit.supermarket.dto.report.CustomerSalesReportResponseDTO;
import iuh.fit.supermarket.dto.report.SalesDailyReportRequestDTO;
import iuh.fit.supermarket.dto.report.SalesDailyReportResponseDTO;

/**
 * Service xử lý các báo cáo
 */
public interface ReportService {

    /**
     * Lấy báo cáo doanh số bán hàng theo ngày
     * Nhóm theo nhân viên bán hàng và ngày bán
     *
     * @param request thông tin filter báo cáo
     * @return dữ liệu báo cáo đã được tổng hợp
     */
    SalesDailyReportResponseDTO getSalesDailyReport(SalesDailyReportRequestDTO request);

    /**
     * Lấy báo cáo doanh số theo khách hàng
     * Nhóm theo khách hàng và nhóm sản phẩm
     * Chỉ tính các đơn hàng đã thanh toán (PAID)
     *
     * @param request thông tin filter báo cáo
     * @return dữ liệu báo cáo doanh số khách hàng đã được tổng hợp
     */
    CustomerSalesReportResponseDTO getCustomerSalesReport(CustomerSalesReportRequestDTO request);
}
