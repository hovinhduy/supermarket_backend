package iuh.fit.supermarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.dto.favorite.AddFavoriteRequest;
import iuh.fit.supermarket.dto.favorite.CustomerFavoriteResponse;
import iuh.fit.supermarket.entity.Customer;
import iuh.fit.supermarket.entity.User;
import iuh.fit.supermarket.repository.CustomerRepository;
import iuh.fit.supermarket.repository.UserRepository;
import iuh.fit.supermarket.service.CustomerFavoriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý các API liên quan đến sản phẩm yêu thích của khách hàng
 */
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Favorites", description = "API quản lý sản phẩm yêu thích của khách hàng")
public class CustomerFavoriteController {

    private final CustomerFavoriteService customerFavoriteService;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    /**
     * Lấy danh sách sản phẩm yêu thích của khách hàng hiện tại
     *
     * @return Danh sách sản phẩm yêu thích
     */
    @Operation(summary = "Lấy danh sách sản phẩm yêu thích",
               description = "Lấy danh sách tất cả sản phẩm yêu thích của khách hàng hiện tại")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerFavoriteResponse>>> getFavorites() {
        try {
            Integer customerId = getCurrentCustomerId();
            List<CustomerFavoriteResponse> favorites = customerFavoriteService.getFavorites(customerId);
            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách yêu thích thành công", favorites));
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách yêu thích", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy danh sách yêu thích: " + e.getMessage()));
        }
    }

    /**
     * Thêm sản phẩm vào danh sách yêu thích
     *
     * @param request Thông tin sản phẩm cần thêm
     * @return Thông báo kết quả
     */
    @Operation(summary = "Thêm sản phẩm yêu thích",
               description = "Thêm một sản phẩm vào danh sách yêu thích của khách hàng")
    @PostMapping
    public ResponseEntity<ApiResponse<String>> addFavorite(
            @Valid @RequestBody AddFavoriteRequest request
    ) {
        try {
            Integer customerId = getCurrentCustomerId();
            String message = customerFavoriteService.addFavorite(customerId, request.productUnitId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(message, null));
        } catch (IllegalArgumentException e) {
            log.error("Lỗi validation khi thêm sản phẩm yêu thích", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi hệ thống khi thêm sản phẩm yêu thích", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Xóa sản phẩm khỏi danh sách yêu thích
     *
     * @param productUnitId ID của product unit
     * @return Thông báo kết quả
     */
    @Operation(summary = "Xóa sản phẩm yêu thích",
               description = "Xóa một sản phẩm khỏi danh sách yêu thích")
    @DeleteMapping("/{productUnitId}")
    public ResponseEntity<ApiResponse<String>> removeFavorite(
            @PathVariable Long productUnitId
    ) {
        try {
            Integer customerId = getCurrentCustomerId();
            String message = customerFavoriteService.removeFavorite(customerId, productUnitId);
            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (IllegalArgumentException e) {
            log.error("Lỗi validation khi xóa sản phẩm yêu thích", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi hệ thống khi xóa sản phẩm yêu thích", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi hệ thống: " + e.getMessage()));
        }
    }

    /**
     * Kiểm tra sản phẩm có trong danh sách yêu thích không
     *
     * @param productUnitId ID của product unit
     * @return true nếu sản phẩm đã có trong danh sách yêu thích
     */
    @Operation(summary = "Kiểm tra sản phẩm yêu thích",
               description = "Kiểm tra xem sản phẩm có trong danh sách yêu thích hay không")
    @GetMapping("/check/{productUnitId}")
    public ResponseEntity<ApiResponse<Boolean>> isFavorite(
            @PathVariable Long productUnitId
    ) {
        try {
            Integer customerId = getCurrentCustomerId();
            boolean isFavorite = customerFavoriteService.isFavorite(customerId, productUnitId);
            return ResponseEntity.ok(ApiResponse.success("Kiểm tra thành công", isFavorite));
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra sản phẩm yêu thích", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi kiểm tra: " + e.getMessage()));
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
        String username = userDetails.getUsername();

        log.debug("Lấy thông tin customer với username: {}", username);

        // Loại bỏ prefix "CUSTOMER:" nếu có
        final String emailOrPhone = username.startsWith("CUSTOMER:")
                ? username.substring(9)  // Bỏ "CUSTOMER:" prefix
                : username;

        if (username.startsWith("CUSTOMER:")) {
            log.debug("Đã loại bỏ prefix, email/phone: {}", emailOrPhone);
        }

        // Tìm User từ email hoặc phone
        User user = userRepository.findByEmailAndIsDeletedFalse(emailOrPhone)
                .or(() -> userRepository.findByPhoneAndIsDeletedFalse(emailOrPhone))
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy user với email/phone: " + emailOrPhone));

        // Tìm Customer từ user_id
        Customer customer = customerRepository.findByUser_UserId(user.getUserId())
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy customer với user_id: " + user.getUserId()));

        return customer.getCustomerId();
    }
}
