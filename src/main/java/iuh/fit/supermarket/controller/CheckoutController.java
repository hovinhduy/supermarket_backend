package iuh.fit.supermarket.controller;

import iuh.fit.supermarket.dto.checkout.CheckoutRequestDTO;
import iuh.fit.supermarket.dto.checkout.CheckoutResponseDTO;
import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.enums.OrderStatus;
import iuh.fit.supermarket.service.CheckoutService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các API liên quan đến checkout và đơn hàng
 */
@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
@Slf4j
public class CheckoutController {

    private final CheckoutService checkoutService;

    /**
     * API checkout giỏ hàng cho khách hàng
     * - Khách hàng tự checkout giỏ hàng của mình
     * - Hỗ trợ 2 loại nhận hàng: tại cửa hàng hoặc giao hàng tận nơi
     * - Hỗ trợ 3 phương thức thanh toán: CASH (tiền mặt/COD), CARD (thẻ), ONLINE (trực tuyến)
     * - Tự động tính phí vận chuyển cho giao hàng tận nơi
     * - Áp dụng khuyến mãi nếu có
     *
     * @param request thông tin checkout
     * @param authentication thông tin xác thực của khách hàng
     * @return thông tin đơn hàng đã tạo
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CheckoutResponseDTO>> checkout(
            @Valid @RequestBody CheckoutRequestDTO request,
            Authentication authentication) {

        String username = authentication.getName();
        log.info("Nhận yêu cầu checkout - Customer: {}, DeliveryType: {}, PaymentMethod: {}",
                username, request.deliveryType(), request.paymentMethod());

        CheckoutResponseDTO response = checkoutService.checkoutForCustomer(username, request);

        log.info("Checkout thành công - Order ID: {}, Total: {}",
                response.orderId(), response.totalAmount());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đặt hàng thành công", response));
    }

    /**
     * API lấy danh sách đơn hàng của khách hàng với phân trang
     * - Khách hàng chỉ xem được đơn hàng của chính mình
     * - Có thể lọc theo trạng thái đơn hàng
     * - Sắp xếp theo ngày đặt hàng mới nhất
     * - Hỗ trợ phân trang
     *
     * @param authentication thông tin xác thực của khách hàng
     * @param status trạng thái đơn hàng cần lọc (optional)
     * @param page số trang (bắt đầu từ 0, mặc định 0)
     * @param size số lượng đơn hàng mỗi trang (mặc định 10)
     * @return danh sách đơn hàng có phân trang
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CheckoutResponseDTO>>> getCustomerOrders(
            Authentication authentication,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String username = authentication.getName();
        log.info("Lấy danh sách đơn hàng - Customer: {}, Status: {}, Page: {}, Size: {}", 
                username, status, page, size);

        // Tạo Pageable với sort theo orderDate giảm dần (mới nhất)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDate"));
        
        Page<CheckoutResponseDTO> orders = checkoutService.getCustomerOrders(username, status, pageable);

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy danh sách đơn hàng thành công", orders));
    }

    /**
     * API lấy thông tin chi tiết đơn hàng của khách hàng
     * - Chỉ cho phép xem đơn hàng của chính mình
     *
     * @param orderId ID đơn hàng
     * @param authentication thông tin xác thực của khách hàng
     * @return thông tin chi tiết đơn hàng
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<CheckoutResponseDTO>> getOrderDetail(
            @PathVariable Long orderId,
            Authentication authentication) {

        String username = authentication.getName();
        log.info("Khách hàng {} lấy thông tin chi tiết đơn hàng ID: {}", username, orderId);

        CheckoutResponseDTO response = checkoutService.getOrderDetailForCustomer(username, orderId);

        return ResponseEntity.ok(ApiResponse.success(
                "Lấy thông tin đơn hàng thành công", response));
    }
}