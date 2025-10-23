package iuh.fit.supermarket.service;

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
}
