package iuh.fit.supermarket.controller;

import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.dto.return_invoice.*;
import iuh.fit.supermarket.service.ReturnInvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controller xử lý các API liên quan đến trả hàng
 */
@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
@Slf4j
public class ReturnInvoiceController {

    private final ReturnInvoiceService returnInvoiceService;

    /**
     * Tính toán preview số tiền hoàn trước khi tạo phiếu trả
     *
     * @param request Request tính toán
     * @return Response với thông tin tính toán
     */
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<RefundCalculationResponse>> calculateRefund(
            @Valid @RequestBody CreateRefundRequest request) {
        log.info("Nhận request tính toán preview trả hàng cho invoice: {}", request.invoiceId());

        RefundCalculationResponse response = returnInvoiceService.calculateRefund(
                request.invoiceId(),
                request.refundLineItems());

        return ResponseEntity.ok(ApiResponse.success("Tính toán trả hàng thành công", response));
    }

    /**
     * Tạo phiếu trả hàng thực tế (lưu DB + cộng kho + hoàn tiền)
     *
     * @param request Request tạo phiếu trả
     * @return Response với thông tin phiếu trả đã tạo
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CreateRefundResponse>> createRefund(
            @Valid @RequestBody CreateRefundRequest request) {
        log.info("Nhận request tạo phiếu trả hàng cho invoice: {}", request.invoiceId());

        CreateRefundResponse response = returnInvoiceService.createRefund(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo phiếu trả hàng thành công", response));
    }

    /**
     * Lấy danh sách hóa đơn trả hàng với tìm kiếm và lọc
     *
     * @param returnCode Mã trả hàng
     * @param invoiceNumber Mã hóa đơn gốc
     * @param customerName Tên khách hàng
     * @param customerPhone Số điện thoại khách hàng
     * @param fromDate Từ ngày
     * @param toDate Đến ngày
     * @param employeeId ID nhân viên
     * @param customerId ID khách hàng
     * @param page Số trang (mặc định 0)
     * @param size Kích thước trang (mặc định 10)
     * @param sortBy Sắp xếp theo (mặc định returnDate)
     * @param sortDirection Hướng sắp xếp (mặc định DESC)
     * @return Response với danh sách phiếu trả
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReturnInvoiceListResponse>>> searchAndFilterReturns(
            @RequestParam(required = false) String returnCode,
            @RequestParam(required = false) String invoiceNumber,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String customerPhone,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(required = false) Integer customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "returnDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("Tìm kiếm phiếu trả: returnCode={}, invoiceNumber={}, customerName={}, customerPhone={}",
                returnCode, invoiceNumber, customerName, customerPhone);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ReturnInvoiceListResponse> response = returnInvoiceService.searchAndFilterReturns(
                returnCode,
                invoiceNumber,
                customerName,
                customerPhone,
                fromDate,
                toDate,
                employeeId,
                customerId,
                pageable);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phiếu trả thành công", response));
    }

    /**
     * Lấy thông tin chi tiết phiếu trả
     *
     * @param returnId ID phiếu trả
     * @return Response với thông tin phiếu trả
     */
    @GetMapping("/{returnId}")
    public ResponseEntity<ApiResponse<CreateRefundResponse>> getReturnInvoice(@PathVariable Integer returnId) {
        log.info("Nhận request lấy thông tin phiếu trả: {}", returnId);

        CreateRefundResponse response = returnInvoiceService.getReturnInvoice(returnId);

        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin phiếu trả thành công", response));
    }

    /**
     * Lấy thông tin chi tiết đầy đủ của phiếu trả
     *
     * @param returnId ID phiếu trả
     * @return Response với thông tin chi tiết phiếu trả
     */
    @GetMapping("/{returnId}/detail")
    public ResponseEntity<ApiResponse<ReturnInvoiceDetailResponse>> getReturnInvoiceDetail(
            @PathVariable Integer returnId) {
        log.info("Nhận request lấy chi tiết đầy đủ phiếu trả: {}", returnId);

        ReturnInvoiceDetailResponse response = returnInvoiceService.getReturnInvoiceDetail(returnId);

        return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết phiếu trả thành công", response));
    }
}
