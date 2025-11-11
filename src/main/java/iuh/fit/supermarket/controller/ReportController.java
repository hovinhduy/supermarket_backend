package iuh.fit.supermarket.controller;

import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.dto.report.CustomerSalesReportRequestDTO;
import iuh.fit.supermarket.dto.report.CustomerSalesReportResponseDTO;
import iuh.fit.supermarket.dto.report.PromotionReportRequestDTO;
import iuh.fit.supermarket.dto.report.PromotionReportResponseDTO;
import iuh.fit.supermarket.dto.report.ReturnReportRequestDTO;
import iuh.fit.supermarket.dto.report.ReturnReportResponseDTO;
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

    /**
     * API lấy báo cáo doanh số theo khách hàng
     * - Nhóm theo khách hàng và nhóm sản phẩm (category)
     * - Hiển thị chiết khấu, doanh số trước/sau chiết khấu
     * - Chỉ tính hóa đơn PAID
     * - Filter: từ ngày - đến ngày, khách hàng (optional)
     *
     * @param request thông tin filter báo cáo
     * @return dữ liệu báo cáo doanh số khách hàng đã tổng hợp
     */
    @PostMapping("/customer-sales")
    public ResponseEntity<ApiResponse<CustomerSalesReportResponseDTO>> getCustomerSalesReport(
            @Valid @RequestBody CustomerSalesReportRequestDTO request) {

        log.info("Nhận yêu cầu báo cáo doanh số khách hàng từ {} đến {}, khách hàng ID: {}",
                request.fromDate(), request.toDate(), request.customerId());

        CustomerSalesReportResponseDTO response = reportService.getCustomerSalesReport(request);

        log.info("Trả về báo cáo doanh số khách hàng: {} khách hàng, tổng: {}",
                response.customerSalesList().size(), response.grandTotalRevenueAfterDiscount());

        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo doanh số khách hàng thành công", response));
    }

    /**
     * API lấy báo cáo trả hàng
     * - Hiển thị chi tiết hóa đơn trả hàng (bao gồm thông tin hóa đơn mua)
     * - Hiển thị mã sản phẩm, tên sản phẩm, nhóm sản phẩm, số lượng, đơn giá, thành tiền
     * - Filter: từ ngày - đến ngày
     *
     * @param request thông tin filter báo cáo
     * @return dữ liệu báo cáo trả hàng đã tổng hợp
     */
    @PostMapping("/returns")
    public ResponseEntity<ApiResponse<ReturnReportResponseDTO>> getReturnReport(
            @Valid @RequestBody ReturnReportRequestDTO request) {

        log.info("Nhận yêu cầu báo cáo trả hàng từ {} đến {}",
                request.fromDate(), request.toDate());

        ReturnReportResponseDTO response = reportService.getReturnReport(request);

        log.info("Trả về báo cáo trả hàng: {} sản phẩm, tổng tiền: {}",
                response.returnItems().size(), response.totalRefundAmount());

        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo trả hàng thành công", response));
    }

    /**
     * API lấy báo cáo tổng kết chương trình khuyến mãi
     * - Hiển thị thông tin các chương trình khuyến mãi
     * - Bao gồm ngày bắt đầu, kết thúc, loại khuyến mãi
     * - Thông tin sản phẩm tặng (nếu là loại Mua X Tặng Y)
     * - Số tiền chiết khấu (nếu là loại giảm giá)
     * - Ngân sách tổng, đã sử dụng, còn lại
     * - Filter: từ ngày - đến ngày, mã CTKM (optional)
     *
     * @param request thông tin filter báo cáo
     * @return dữ liệu báo cáo khuyến mãi đã tổng hợp
     */
    @PostMapping("/promotions")
    public ResponseEntity<ApiResponse<PromotionReportResponseDTO>> getPromotionReport(
            @Valid @RequestBody PromotionReportRequestDTO request) {

        log.info("Nhận yêu cầu báo cáo khuyến mãi từ {} đến {}, mã CTKM: {}",
                request.fromDate(), request.toDate(), request.promotionCode());

        PromotionReportResponseDTO response = reportService.getPromotionReport(request);

        log.info("Trả về báo cáo khuyến mãi: {} CTKM, tổng ngân sách: {}",
                response.promotionList().size(), response.totalBudget());

        return ResponseEntity.ok(ApiResponse.success("Lấy báo cáo khuyến mãi thành công", response));
    }
}
