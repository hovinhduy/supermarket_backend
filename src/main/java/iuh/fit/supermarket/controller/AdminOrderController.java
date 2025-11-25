package iuh.fit.supermarket.controller;

import iuh.fit.supermarket.dto.checkout.CheckoutResponseDTO;
import iuh.fit.supermarket.dto.checkout.UpdateOrderStatusDTO;
import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.enums.OrderStatus;
import iuh.fit.supermarket.enums.DeliveryType;
import iuh.fit.supermarket.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các API quản lý đơn hàng dành cho Admin
 * Yêu cầu quyền ADMIN để truy cập tất cả các endpoint
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

        private final CheckoutService checkoutService;

        /**
         * API lấy danh sách tất cả đơn hàng với khả năng lọc theo trạng thái và loại hình nhận hàng
         * - Admin có thể xem tất cả đơn hàng trong hệ thống
         * - Có thể lọc theo trạng thái đơn hàng
         * - Có thể lọc theo loại hình nhận hàng (nhận tại siêu thị hoặc giao hàng)
         * - Hỗ trợ phân trang và sắp xếp
         * - Mặc định sắp xếp theo ngày đặt hàng mới nhất
         *
         * @param status        trạng thái đơn hàng cần lọc (optional)
         * @param deliveryType  loại hình nhận hàng: PICKUP_AT_STORE hoặc HOME_DELIVERY (optional)
         * @param page          số trang (bắt đầu từ 0, mặc định 0)
         * @param size          số lượng đơn hàng mỗi trang (mặc định 20)
         * @param sortBy        trường sắp xếp (mặc định "orderDate")
         * @param sortDirection hướng sắp xếp ASC hoặc DESC (mặc định DESC)
         * @return danh sách đơn hàng có phân trang
         */
        @GetMapping
        public ResponseEntity<ApiResponse<Page<CheckoutResponseDTO>>> getAllOrders(
                        @RequestParam(required = false) OrderStatus status,
                        @RequestParam(required = false) DeliveryType deliveryType,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(defaultValue = "orderDate") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDirection) {

                log.info("Admin lấy danh sách đơn hàng - Status: {}, DeliveryType: {}, Page: {}, Size: {}, SortBy: {}, Direction: {}",
                                status, deliveryType, page, size, sortBy, sortDirection);

                // Xác định hướng sắp xếp
                Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC")
                                ? Sort.Direction.ASC
                                : Sort.Direction.DESC;

                // Tạo Pageable với sort
                Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

                // Gọi service để lấy danh sách đơn hàng với lọc theo deliveryType
                Page<CheckoutResponseDTO> orders = checkoutService.getAllOrders(status, deliveryType, pageable);

                log.info("Tìm thấy {} đơn hàng, trang {}/{}",
                                orders.getNumberOfElements(), page + 1, orders.getTotalPages());

                return ResponseEntity.ok(ApiResponse.success(
                                "Lấy danh sách đơn hàng thành công", orders));
        }

        /**
         * API lấy thông tin chi tiết đơn hàng
         * - Admin có thể xem chi tiết bất kỳ đơn hàng nào
         *
         * @param orderId ID đơn hàng
         * @return thông tin chi tiết đơn hàng
         */
        @GetMapping("/{orderId}")
        public ResponseEntity<ApiResponse<CheckoutResponseDTO>> getOrderDetail(
                        @PathVariable Long orderId) {

                log.info("Admin lấy thông tin chi tiết đơn hàng ID: {}", orderId);

                CheckoutResponseDTO response = checkoutService.getOrderDetail(orderId);

                return ResponseEntity.ok(ApiResponse.success(
                                "Lấy thông tin đơn hàng thành công", response));
        }

        /**
         * API cập nhật trạng thái đơn hàng
         * - Chỉ Admin mới có quyền thay đổi trạng thái đơn hàng
         * - Kiểm tra tính hợp lệ của việc chuyển trạng thái
         * - Ghi log lịch sử thay đổi trạng thái
         *
         * @param orderId ID đơn hàng
         * @param request thông tin cập nhật trạng thái
         * @return thông tin đơn hàng sau khi cập nhật
         */
        @PutMapping("/{orderId}/status")
        public ResponseEntity<ApiResponse<CheckoutResponseDTO>> updateOrderStatus(
                        @PathVariable Long orderId,
                        @Valid @RequestBody UpdateOrderStatusDTO request) {

                log.info("Admin cập nhật trạng thái đơn hàng ID: {} sang trạng thái: {}",
                                orderId, request.newStatus());

                // Gọi service để cập nhật trạng thái
                CheckoutResponseDTO response = checkoutService.updateOrderStatus(
                                orderId, request.newStatus());

                log.info("Cập nhật trạng thái đơn hàng ID: {} thành công", orderId);

                return ResponseEntity.ok(ApiResponse.success(
                                "Cập nhật trạng thái đơn hàng thành công", response));
        }

        /**
         * API hủy đơn hàng
         * - Admin có thể hủy bất kỳ đơn hàng nào chưa hoàn thành
         * - Lý do hủy đơn hàng là tùy chọn, nếu không cung cấp sẽ dùng giá trị mặc định
         *
         * @param orderId ID đơn hàng
         * @param reason  lý do hủy đơn hàng (optional, mặc định: "Không có lý do")
         * @return thông tin đơn hàng sau khi hủy
         */
        @PostMapping("/{orderId}/cancel")
        public ResponseEntity<ApiResponse<CheckoutResponseDTO>> cancelOrder(
                        @PathVariable Long orderId,
                        @RequestParam(required = false, defaultValue = "Không có lý do") String reason) {

                log.info("Admin hủy đơn hàng ID: {}, Lý do: {}", orderId, reason);

                CheckoutResponseDTO response = checkoutService.cancelOrder(orderId, reason);

                log.info("Hủy đơn hàng ID: {} thành công", orderId);

                return ResponseEntity.ok(ApiResponse.success(
                                "Hủy đơn hàng thành công", response));
        }

}