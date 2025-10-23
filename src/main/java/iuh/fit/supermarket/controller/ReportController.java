package iuh.fit.supermarket.controller;

import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.dto.report.SalesDailyReportRequestDTO;
import iuh.fit.supermarket.dto.report.SalesDailyReportResponseDTO;
import iuh.fit.supermarket.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các API liên quan đến báo cáo
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;

    /**
     * API lấy báo cáo doanh số bán hàng theo ngày
     * - Nhóm theo nhân viên bán hàng và ngày bán
     * - Hiển thị chiết khấu, doanh số trước/sau chiết khấu
     * - Chỉ tính hóa đơn PAID
     * - Filter: từ ngày - đến ngày, nhân viên (optional)
     *
     * @param request thông tin filter báo cáo
     * @return dữ liệu báo cáo doanh số đã tổng hợp
     */
    @PostMapping("/sales-daily")
    public ResponseEntity<ApiResponse<SalesDailyReportResponseDTO>> getSalesDailyReport(
            @Valid @RequestBody SalesDailyReportRequestDTO request) {

        log.info("Nhận yêu cầu báo cáo doanh số từ {} đến {}, nhân viên ID: {}",
                request.fromDate(), request.toDate(), request.employeeId());

        SalesDailyReportResponseDTO response = reportService.getSalesDailyReport(request);

        log.info("Trả về báo cáo doanh số: {} nhân viên, tổng: {}",
                response.employeeSalesList().size(), response.grandTotalRevenueAfterDiscount());

        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo doanh số thành công", response));
    }
}
