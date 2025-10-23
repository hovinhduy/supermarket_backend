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
     * @param searchKeyword Từ khóa tìm kiếm (tìm trong mã trả hàng, mã hóa đơn gốc, tên khách hàng, số điện thoại)
     * @param fromDate Từ ngày
     * @param toDate Đến ngày
     * @param employeeId ID nhân viên
     * @param customerId ID khách hàng
     * @param productUnitId ID sản phẩm đơn vị
     * @param page Số trang (mặc định 0)
     * @param size Kích thước trang (mặc định 10)
     * @param sortBy Sắp xếp theo (mặc định returnDate)
     * @param sortDirection Hướng sắp xếp (mặc định DESC)
     * @return Response với danh sách phiếu trả
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReturnInvoiceListResponse>>> searchAndFilterReturns(
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(required = false) Integer customerId,
            @RequestParam(required = false) Integer productUnitId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "returnDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("Tìm kiếm phiếu trả: searchKeyword={}, fromDate={}, toDate={}, employeeId={}, customerId={}, productUnitId={}",
                searchKeyword, fromDate, toDate, employeeId, customerId, productUnitId);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ReturnInvoiceListResponse> response = returnInvoiceService.searchAndFilterReturns(
                searchKeyword,
                fromDate,
                toDate,
                employeeId,
                customerId,
                productUnitId,
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

    /**
     * Kiểm tra số lượng còn lại có thể trả của mỗi hóa đơn bán
     *
     * @param invoiceId ID hóa đơn bán
     * @return Response với thông tin số lượng có thể trả
     */
    @GetMapping("/invoice/{invoiceId}/available-quantity")
    public ResponseEntity<ApiResponse<AvailableReturnQuantityResponse>> getAvailableReturnQuantity(
            @PathVariable Integer invoiceId) {
        log.info("Nhận request kiểm tra số lượng có thể trả cho hóa đơn: {}", invoiceId);

        AvailableReturnQuantityResponse response = returnInvoiceService.getAvailableReturnQuantity(invoiceId);

        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin số lượng có thể trả thành công", response));
    }
}
