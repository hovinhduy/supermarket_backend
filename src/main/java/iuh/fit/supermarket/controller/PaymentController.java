package iuh.fit.supermarket.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.model.webhooks.WebhookData;

import java.time.LocalDateTime;

/**
 * Controller xử lý webhook từ PayOS
 */
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PayOS payOS;
    private final PaymentService paymentService;

    /**
     * Webhook endpoint duy nhất nhận thông báo từ PayOS
     * Tự động phân biệt và xử lý cho cả Invoice và Order
     */
    @PostMapping("/payos_transfer_handler")
    public ResponseEntity<ApiResponse<WebhookData>> payosWebhookHandler(@RequestBody Object body)
            throws JsonProcessingException {
        try {
            WebhookData data = payOS.webhooks().verify(body);
            log.info("Nhận webhook từ PayOS cho order code: {}", data.getOrderCode());

            if ("00".equals(data.getCode())) {
                // Tạo transaction ID từ webhook data
                String transactionId = data.getTransactionDateTime() != null
                        ? String.valueOf(data.getTransactionDateTime())
                        : "PAYOS_" + data.getOrderCode();

                // Gọi PaymentService để xử lý - service sẽ tự động phân biệt Invoice hay Order
                paymentService.handlePaymentWebhook(data.getOrderCode(), transactionId);

                log.info("Xử lý thanh toán thành công cho order code: {}, transaction: {}",
                        data.getOrderCode(), transactionId);

                return ResponseEntity.ok(ApiResponse.<WebhookData>builder()
                        .success(true)
                        .message("Webhook xử lý thanh toán thành công")
                        .data(data)
                        .timestamp(LocalDateTime.now())
                        .build());
            } else {
                log.warn("Webhook nhận được với status code không thành công: {}", data.getCode());
                return ResponseEntity.ok(ApiResponse.<WebhookData>builder()
                        .success(false)
                        .message("Thanh toán không thành công: " + data.getDesc())
                        .data(data)
                        .timestamp(LocalDateTime.now())
                        .build());
            }

        } catch (Exception e) {
            log.error("Lỗi khi xử lý webhook: {}", e.getMessage(), e);
            return ResponseEntity.ok(ApiResponse.<WebhookData>builder()
                    .success(false)
                    .message("Lỗi khi xử lý webhook: " + e.getMessage())
                    .timestamp(LocalDateTime.now())
                    .build());
        }
    }

    /**
     * Endpoint xử lý redirect khi khách hàng thanh toán thành công
     */
    @GetMapping("/return")
    public ResponseEntity<ApiResponse<String>> handlePaymentReturn(
            @RequestParam(required = false) Long orderCode,
            @RequestParam(required = false) String status) {
        log.info("Khách hàng quay lại sau thanh toán. Order code: {}, Status: {}", orderCode, status);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Thanh toán đang được xử lý")
                .data("Vui lòng chờ xác nhận thanh toán từ hệ thống")
                .timestamp(LocalDateTime.now())
                .build());
    }

    /**
     * Endpoint xử lý redirect khi khách hàng hủy thanh toán
     */
    @GetMapping("/cancel")
    public ResponseEntity<ApiResponse<String>> handlePaymentCancel(
            @RequestParam(required = false) Long orderCode) {
        log.info("Khách hàng hủy thanh toán. Order code: {}", orderCode);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(false)
                .message("Thanh toán đã bị hủy")
                .data("Order code: " + orderCode)
                .timestamp(LocalDateTime.now())
                .build());
    }
}
