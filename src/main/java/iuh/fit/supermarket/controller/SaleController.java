package iuh.fit.supermarket.controller;

import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.dto.sale.*;
import iuh.fit.supermarket.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các API liên quan đến bán hàng
 */
@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@Slf4j
public class SaleController {

    private final SaleService saleService;

    /**
     * API tạo bán hàng mới
     * - Kiểm tra tồn kho
     * - Tạo order và invoice
     * - Lưu thông tin khuyến mãi đã áp dụng
     * - CASH/CARD: trừ kho ngay và invoice PAID
     * - ONLINE: invoice ISSUED, trừ kho khi webhook confirm
     * 
     * @param request thông tin bán hàng
     * @return thông tin hóa đơn đã tạo
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CreateSaleResponseDTO>> createSale(
            @Valid @RequestBody CreateSaleRequestDTO request) {
        
        log.info("Nhận yêu cầu bán hàng từ nhân viên ID: {}", request.employeeId());

        CreateSaleResponseDTO response = saleService.createSale(request);
        
        log.info("Tạo bán hàng thành công. Invoice: {}", response.invoiceNumber());
        
        return ResponseEntity.ok(ApiResponse.success("Tạo bán hàng thành công", response));
    }

    /**
     * API lấy trạng thái đơn hàng
     * - Dùng để polling kiểm tra order đã COMPLETED chưa
     * - Dùng cho thanh toán ONLINE
     * 
     * @param orderId ID của đơn hàng
     * @return thông tin trạng thái đơn hàng
     */
    @GetMapping("/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderStatusResponseDTO>> getOrderStatus(
            @PathVariable Long orderId) {
        
        log.info("Kiểm tra trạng thái đơn hàng ID: {}", orderId);

        OrderStatusResponseDTO response = saleService.getOrderStatus(orderId);
        
        return ResponseEntity.ok(ApiResponse.success("Lấy trạng thái đơn hàng thành công", response));
    }

    /**
     * API lấy danh sách hoá đơn bán có đầy đủ thông tin khuyến mãi được áp dụng
     * - Trả về danh sách hoá đơn với các khuyến mãi item-level và order-level
     * - Hỗ trợ phân trang
     * 
     * @param pageNumber số trang (mặc định 0)
     * @param pageSize kích thước trang (mặc định 10)
     * @return danh sách hoá đơn với thông tin khuyến mãi đầy đủ
     */
    @GetMapping
    public ResponseEntity<ApiResponse<SaleInvoicesListResponseDTO>> getSalesInvoicesWithPromotions(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        log.info("Lấy danh sách hoá đơn bán với trang: {}, kích thước: {}", pageNumber, pageSize);

        SaleInvoicesListResponseDTO response = saleService.getSalesInvoicesWithPromotions(pageNumber, pageSize);
        
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách hoá đơn thành công", response));
    }

    /**
     * API tìm kiếm và lọc danh sách hoá đơn bán
     * - Tìm kiếm theo mã hoá đơn
     * - Tìm kiếm theo tên khách hàng
     * - Lọc theo khoảng ngày (từ ngày - đến ngày)
     * - Lọc theo trạng thái hoá đơn
     * - Hỗ trợ phân trang
     * 
     * @param invoiceNumber mã hoá đơn (optional)
     * @param customerName tên khách hàng (optional)
     * @param fromDate từ ngày (optional, format: yyyy-MM-dd)
     * @param toDate đến ngày (optional, format: yyyy-MM-dd)
     * @param status trạng thái hoá đơn (optional)
     * @param pageNumber số trang (mặc định 0)
     * @param pageSize kích thước trang (mặc định 10)
     * @return danh sách hoá đơn phù hợp với tiêu chí tìm kiếm
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<SaleInvoicesListResponseDTO>> searchSalesInvoices(
            @RequestParam(required = false) String invoiceNumber,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        log.info("Tìm kiếm hoá đơn - Invoice: {}, Customer: {}, From: {}, To: {}, Status: {}", 
                invoiceNumber, customerName, fromDate, toDate, status);

        SaleInvoiceSearchRequestDTO searchRequest = new SaleInvoiceSearchRequestDTO(
                invoiceNumber,
                customerName,
                fromDate != null ? java.time.LocalDate.parse(fromDate) : null,
                toDate != null ? java.time.LocalDate.parse(toDate) : null,
                status != null ? iuh.fit.supermarket.enums.InvoiceStatus.valueOf(status) : null,
                pageNumber,
                pageSize
        );

        SaleInvoicesListResponseDTO response = saleService.searchSalesInvoices(searchRequest);
        
        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm hoá đơn thành công", response));
    }

    /**
     * API lấy thông tin chi tiết hoá đơn bán
     * - Trả về hoá đơn với đầy đủ thông tin items và khuyến mãi
     * 
     * @param invoiceId ID của hoá đơn
     * @return thông tin chi tiết hoá đơn
     */
    @GetMapping("/{invoiceId}")
    public ResponseEntity<ApiResponse<SaleInvoiceFullDTO>> getInvoiceDetail(
            @PathVariable Integer invoiceId) {
        
        log.info("Lấy thông tin chi tiết hoá đơn ID: {}", invoiceId);

        SaleInvoiceFullDTO response = saleService.getInvoiceDetail(invoiceId);
        
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin hoá đơn thành công", response));
    }
}
