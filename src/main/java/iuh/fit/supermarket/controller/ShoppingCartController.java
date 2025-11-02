package iuh.fit.supermarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import iuh.fit.supermarket.dto.cart.AddCartItemRequest;
import iuh.fit.supermarket.dto.cart.CartResponse;
import iuh.fit.supermarket.dto.cart.UpdateCartItemRequest;
import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.entity.Customer;
import iuh.fit.supermarket.entity.User;
import iuh.fit.supermarket.repository.CustomerRepository;
import iuh.fit.supermarket.repository.UserRepository;
import iuh.fit.supermarket.service.ShoppingCartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các API liên quan đến giỏ hàng
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Shopping Cart", description = "API quản lý giỏ hàng")
@SecurityRequirement(name = "bearerAuth")
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    /**
     * Lấy giỏ hàng của khách hàng hiện tại
     *
     * @return Thông tin giỏ hàng
     */
    @Operation(summary = "Lấy giỏ hàng", description = "Lấy thông tin giỏ hàng của khách hàng hiện tại")
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        try {
            Integer customerId = getCurrentCustomerId();
            CartResponse cart = shoppingCartService.getCart(customerId);
            return ResponseEntity.ok(ApiResponse.success("Lấy giỏ hàng thành công", cart));
        } catch (Exception e) {
            log.error("Lỗi khi lấy giỏ hàng", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy giỏ hàng: " + e.getMessage()));
        }
    }

    /**
     * Thêm sản phẩm vào giỏ hàng
     *
     * @param request Thông tin sản phẩm cần thêm
     * @return Thông tin giỏ hàng sau khi thêm
     */
    @Operation(summary = "Thêm sản phẩm vào giỏ hàng", description = "Thêm một sản phẩm vào giỏ hàng hoặc cập nhật số lượng nếu đã tồn tại")
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItemToCart(
            @Valid @RequestBody AddCartItemRequest request
    ) {
        try {
            Integer customerId = getCurrentCustomerId();
            CartResponse cart = shoppingCartService.addItemToCart(customerId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Thêm sản phẩm vào giỏ hàng thành công", cart));
        } catch (RuntimeException e) {
            log.error("Lỗi khi thêm sản phẩm vào giỏ hàng", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Lỗi khi thêm sản phẩm: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi hệ thống khi thêm sản phẩm vào giỏ hàng", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Cập nhật số lượng sản phẩm trong giỏ hàng
     *
     * @param productUnitId ID của product unit
     * @param request       Thông tin cập nhật
     * @return Thông tin giỏ hàng sau khi cập nhật
     */
    @Operation(summary = "Cập nhật số lượng sản phẩm", description = "Cập nhật số lượng của một sản phẩm trong giỏ hàng")
    @PutMapping("/items/{productUnitId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            @PathVariable Long productUnitId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        try {
            Integer customerId = getCurrentCustomerId();
            CartResponse cart = shoppingCartService.updateCartItem(customerId, productUnitId, request);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật số lượng thành công", cart));
        } catch (RuntimeException e) {
            log.error("Lỗi khi cập nhật số lượng sản phẩm", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Lỗi khi cập nhật: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi hệ thống khi cập nhật số lượng sản phẩm", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Xóa sản phẩm khỏi giỏ hàng
     *
     * @param productUnitId ID của product unit
     * @return Thông tin giỏ hàng sau khi xóa
     */
    @Operation(summary = "Xóa sản phẩm khỏi giỏ hàng", description = "Xóa một sản phẩm khỏi giỏ hàng")
    @DeleteMapping("/items/{productUnitId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItemFromCart(
            @PathVariable Long productUnitId
    ) {
        try {
            Integer customerId = getCurrentCustomerId();
            CartResponse cart = shoppingCartService.removeItemFromCart(customerId, productUnitId);
            return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm khỏi giỏ hàng thành công", cart));
        } catch (RuntimeException e) {
            log.error("Lỗi khi xóa sản phẩm khỏi giỏ hàng", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Lỗi khi xóa: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi hệ thống khi xóa sản phẩm khỏi giỏ hàng", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Xóa toàn bộ giỏ hàng
     *
     * @return Response thành công
     */
    @Operation(summary = "Xóa toàn bộ giỏ hàng", description = "Xóa tất cả sản phẩm trong giỏ hàng")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart() {
        try {
            Integer customerId = getCurrentCustomerId();
            shoppingCartService.clearCart(customerId);
            return ResponseEntity.ok(ApiResponse.success("Xóa toàn bộ giỏ hàng thành công", null));
        } catch (RuntimeException e) {
            log.error("Lỗi khi xóa giỏ hàng", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Lỗi khi xóa giỏ hàng: " + e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi hệ thống khi xóa giỏ hàng", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Lấy customer ID từ SecurityContext
     *
     * @return Customer ID của user hiện tại
     */
    private Integer getCurrentCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Không có thông tin xác thực");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof UserDetails)) {
            throw new IllegalStateException("Principal không phải là UserDetails");
        }

        UserDetails userDetails = (UserDetails) principal;
        String email = userDetails.getUsername();

        log.debug("Lấy thông tin customer với email: {}", email);

        // Tìm User từ email
        User user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy user với email: " + email));

        // Tìm Customer từ user_id
        Customer customer = customerRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy customer với user_id: " + user.getUserId()));

        return customer.getCustomerId();
    }
}
