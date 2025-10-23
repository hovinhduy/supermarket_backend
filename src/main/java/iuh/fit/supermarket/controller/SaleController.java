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
     * API tìm kiếm và lọc danh sách hoá đơn bán có đầy đủ thông tin khuyến mãi
     * - Tìm kiếm theo từ khóa (tìm trong mã hoá đơn và số điện thoại khách hàng)
     * - Lọc theo khoảng ngày (từ ngày - đến ngày)
     * - Lọc theo trạng thái hoá đơn
     * - Lọc theo nhân viên
     * - Lọc theo khách hàng
     * - Lọc theo sản phẩm đơn vị
     * - Hỗ trợ phân trang
     * 
     * @param searchKeyword từ khóa tìm kiếm (optional)
     * @param fromDate từ ngày (optional, format: yyyy-MM-dd)
     * @param toDate đến ngày (optional, format: yyyy-MM-dd)
     * @param status trạng thái hoá đơn (optional)
     * @param employeeId ID nhân viên (optional)
     * @param customerId ID khách hàng (optional)
     * @param productUnitId ID sản phẩm đơn vị (optional)
     * @param pageNumber số trang (mặc định 0)
     * @param pageSize kích thước trang (mặc định 10)
     * @return danh sách hoá đơn với thông tin khuyến mãi đầy đủ
     */
    @GetMapping
    public ResponseEntity<ApiResponse<SaleInvoicesListResponseDTO>> searchAndFilterSalesInvoices(
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fromDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate toDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(required = false) Integer customerId,
            @RequestParam(required = false) Integer productUnitId,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        log.info("Tìm kiếm hoá đơn - Keyword: {}, From: {}, To: {}, Status: {}, EmployeeId: {}, CustomerId: {}, ProductUnitId: {}", 
                searchKeyword, fromDate, toDate, status, employeeId, customerId, productUnitId);

        iuh.fit.supermarket.enums.InvoiceStatus invoiceStatus = null;
        if (status != null && !status.isEmpty()) {
            try {
                invoiceStatus = iuh.fit.supermarket.enums.InvoiceStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                log.warn("Trạng thái hoá đơn không hợp lệ: {}", status);
            }
        }

        SaleInvoicesListResponseDTO response = saleService.searchAndFilterSalesInvoices(
                searchKeyword,
                fromDate,
                toDate,
                invoiceStatus,
                employeeId,
                customerId,
                productUnitId,
                pageNumber,
                pageSize
        );
        
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách hoá đơn thành công", response));
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

    /**
     * API tạo và tải xuống file PDF hóa đơn bán hàng
     * 
     * @param invoiceId ID của hoá đơn
     * @return file PDF
     */
    @GetMapping("/{invoiceId}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Integer invoiceId) {
        log.info("Tạo PDF cho hóa đơn ID: {}", invoiceId);

        byte[] pdfContent = saleService.generateInvoicePdf(invoiceId);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice_" + invoiceId + ".pdf");
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

        return new ResponseEntity<>(pdfContent, headers, org.springframework.http.HttpStatus.OK);
    }

    /**
     * API tạo HTML hóa đơn để in trực tiếp từ trình duyệt
     * 
     * @param invoiceId ID của hoá đơn
     * @return HTML content có thể in trực tiếp (Ctrl+P hoặc nút In)
     */
    @GetMapping("/{invoiceId}/print")
    public ResponseEntity<String> printInvoiceHtml(@PathVariable Integer invoiceId) {
        log.info("Tạo HTML để in cho hóa đơn ID: {}", invoiceId);

        String htmlContent = saleService.generateInvoiceHtml(invoiceId);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.TEXT_HTML);
        headers.add("Content-Type", "text/html; charset=UTF-8");

        return new ResponseEntity<>(htmlContent, headers, org.springframework.http.HttpStatus.OK);
    }
}
