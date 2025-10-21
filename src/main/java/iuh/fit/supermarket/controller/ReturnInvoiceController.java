package iuh.fit.supermarket.controller;

import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.dto.return_invoice.CreateRefundRequest;
import iuh.fit.supermarket.dto.return_invoice.CreateRefundResponse;
import iuh.fit.supermarket.dto.return_invoice.RefundCalculationResponse;
import iuh.fit.supermarket.service.ReturnInvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
