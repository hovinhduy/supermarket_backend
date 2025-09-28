package iuh.fit.supermarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.dto.customer.*;
import iuh.fit.supermarket.enums.CustomerType;
import iuh.fit.supermarket.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý các API liên quan đến khách hàng
 */
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customer Management", description = "API quản lý khách hàng")
@SecurityRequirement(name = "Bearer Authentication")
public class CustomerController {

    private final CustomerService customerService;

    /**
     * Lấy danh sách tất cả khách hàng (chỉ ADMIN và MANAGER)
     */
    @Operation(summary = "Lấy danh sách tất cả khách hàng", description = "Lấy danh sách tất cả khách hàng trong hệ thống. Chỉ ADMIN và MANAGER mới có quyền truy cập.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công", content = @Content(schema = @Schema(implementation = CustomerDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<CustomerDto>>> getAllCustomers() {
        log.info("Nhận yêu cầu lấy danh sách tất cả khách hàng");

        List<CustomerDto> customers = customerService.getAllCustomers();

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách khách hàng thành công", customers));
    }

    /**
     * Lấy danh sách khách hàng với phân trang
     */
    @Operation(summary = "Lấy danh sách khách hàng với phân trang", description = "Lấy danh sách khách hàng với phân trang và sắp xếp")
    @GetMapping("/paginated")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Page<CustomerDto>>> getCustomersWithPagination(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "desc") String sortDirection) {

        log.info("Nhận yêu cầu lấy danh sách khách hàng với phân trang: page={}, size={}, sortBy={}, sortDirection={}",
                page, size, sortBy, sortDirection);

        Page<CustomerDto> customers = customerService.getCustomersWithPagination(page, size, sortBy, sortDirection);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách khách hàng thành công", customers));
    }

    /**
     * Lấy thông tin khách hàng theo ID
     */
    @Operation(summary = "Lấy thông tin khách hàng theo ID", description = "Lấy thông tin chi tiết của một khách hàng theo ID")
    @GetMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<CustomerDto>> getCustomerById(
            @Parameter(description = "ID khách hàng") @PathVariable Integer customerId) {

        log.info("Nhận yêu cầu lấy thông tin khách hàng với ID: {}", customerId);

        CustomerDto customer = customerService.getCustomerById(customerId);

        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin khách hàng thành công", customer));
    }

    /**
     * Lấy thông tin khách hàng theo email
     */
    @Operation(summary = "Lấy thông tin khách hàng theo email", description = "Lấy thông tin chi tiết của một khách hàng theo email")
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<CustomerDto>> getCustomerByEmail(
            @Parameter(description = "Email khách hàng") @PathVariable String email) {

        log.info("Nhận yêu cầu lấy thông tin khách hàng với email: {}", email);

        CustomerDto customer = customerService.getCustomerByEmail(email);

        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin khách hàng thành công", customer));
    }

    /**
     * Lấy thông tin khách hàng theo số điện thoại
     */
    @Operation(summary = "Lấy thông tin khách hàng theo số điện thoại", description = "Lấy thông tin chi tiết của một khách hàng theo số điện thoại")
    @GetMapping("/phone/{phone}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<CustomerDto>> getCustomerByPhone(
            @Parameter(description = "Số điện thoại khách hàng") @PathVariable String phone) {

        log.info("Nhận yêu cầu lấy thông tin khách hàng với phone: {}", phone);

        CustomerDto customer = customerService.getCustomerByPhone(phone);

        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin khách hàng thành công", customer));
    }

    /**
     * Đăng ký khách hàng mới (self-registration) - API công khai
     */
    @Operation(summary = "Đăng ký khách hàng mới", description = "API công khai cho khách hàng tự đăng ký. Nếu số điện thoại đã tồn tại nhưng chưa có mật khẩu, sẽ cập nhật thông tin.")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CustomerDto>> registerCustomer(
            @Valid @RequestBody RegisterCustomerRequest request) {

        log.info("Nhận yêu cầu đăng ký khách hàng mới với email: {}, phone: {}", request.getEmail(),
                request.getPhone());

        CustomerDto customer = customerService.registerCustomer(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đăng ký khách hàng thành công", customer));
    }

    /**
     * Tạo khách hàng mới bởi admin (không yêu cầu mật khẩu)
     */
    @Operation(summary = "Admin tạo khách hàng mới", description = "API dành cho admin/manager tạo khách hàng mới không cần mật khẩu. Khách hàng có thể đăng ký sau để thêm mật khẩu.")
    @PostMapping("/admin-create")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<CustomerDto>> createCustomerByAdmin(
            @Valid @RequestBody CreateCustomerRequest request) {

        log.info("Nhận yêu cầu admin tạo khách hàng mới với email: {}", request.getEmail());

        CustomerDto customer = customerService.createCustomerByAdmin(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo khách hàng thành công", customer));
    }

    /**
     * Cập nhật thông tin khách hàng
     */
    @Operation(summary = "Cập nhật thông tin khách hàng", description = "Cập nhật thông tin của một khách hàng")
    @PutMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<CustomerDto>> updateCustomer(
            @Parameter(description = "ID khách hàng") @PathVariable Integer customerId,
            @Valid @RequestBody UpdateCustomerRequest request) {

        log.info("Nhận yêu cầu cập nhật khách hàng với ID: {}", customerId);

        CustomerDto customer = customerService.updateCustomer(customerId, request);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật khách hàng thành công", customer));
    }

    /**
     * Xóa khách hàng (soft delete)
     */
    @Operation(summary = "Xóa khách hàng", description = "Xóa mềm một khách hàng khỏi hệ thống")
    @DeleteMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<String>> deleteCustomer(
            @Parameter(description = "ID khách hàng") @PathVariable Integer customerId) {

        log.info("Nhận yêu cầu xóa khách hàng với ID: {}", customerId);

        customerService.deleteCustomer(customerId);

        return ResponseEntity.ok(ApiResponse.success(null, "Xóa khách hàng thành công"));
    }

    /**
     * Khôi phục khách hàng đã bị xóa
     */
    @Operation(summary = "Khôi phục khách hàng", description = "Khôi phục một khách hàng đã bị xóa mềm")
    @PostMapping("/{customerId}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> restoreCustomer(
            @Parameter(description = "ID khách hàng") @PathVariable Integer customerId) {

        log.info("Nhận yêu cầu khôi phục khách hàng với ID: {}", customerId);

        customerService.restoreCustomer(customerId);

        return ResponseEntity.ok(ApiResponse.success(null, "Khôi phục khách hàng thành công"));
    }

    /**
     * Đổi mật khẩu khách hàng
     */
    @Operation(summary = "Đổi mật khẩu khách hàng", description = "Đổi mật khẩu cho một khách hàng")
    @PostMapping("/{customerId}/change-password")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Parameter(description = "ID khách hàng") @PathVariable Integer customerId,
            @Valid @RequestBody ChangePasswordRequest request) {

        log.info("Nhận yêu cầu đổi mật khẩu cho khách hàng với ID: {}", customerId);

        customerService.changePassword(customerId, request);

        return ResponseEntity.ok(ApiResponse.success(null, "Đổi mật khẩu thành công"));
    }

    /**
     * Tìm kiếm khách hàng theo nhiều tiêu chí
     */
    @Operation(summary = "Tìm kiếm khách hàng", description = "Tìm kiếm khách hàng theo tên, email, số điện thoại hoặc loại khách hàng")
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<Page<CustomerDto>>> searchCustomers(
            @Valid @RequestBody CustomerSearchRequest request) {

        log.info("Nhận yêu cầu tìm kiếm khách hàng với từ khóa: {}, loại: {}",
                request.getSearchTerm(), request.getCustomerType());

        Page<CustomerDto> customers = customerService.searchCustomers(request);

        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm khách hàng thành công", customers));
    }

    /**
     * Tìm kiếm khách hàng theo tên
     */
    @Operation(summary = "Tìm kiếm khách hàng theo tên", description = "Tìm kiếm khách hàng theo tên (tìm kiếm gần đúng)")
    @GetMapping("/search/name")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<CustomerDto>>> searchCustomersByName(
            @Parameter(description = "Tên khách hàng") @RequestParam String name) {

        log.info("Nhận yêu cầu tìm kiếm khách hàng theo tên: {}", name);

        List<CustomerDto> customers = customerService.searchCustomersByName(name);

        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm khách hàng theo tên thành công", customers));
    }

    /**
     * Tìm kiếm khách hàng theo email
     */
    @Operation(summary = "Tìm kiếm khách hàng theo email", description = "Tìm kiếm khách hàng theo email (tìm kiếm gần đúng)")
    @GetMapping("/search/email")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<CustomerDto>>> searchCustomersByEmail(
            @Parameter(description = "Email khách hàng") @RequestParam String email) {

        log.info("Nhận yêu cầu tìm kiếm khách hàng theo email: {}", email);

        List<CustomerDto> customers = customerService.searchCustomersByEmail(email);

        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm khách hàng theo email thành công", customers));
    }

    /**
     * Tìm kiếm khách hàng theo số điện thoại
     */
    @Operation(summary = "Tìm kiếm khách hàng theo số điện thoại", description = "Tìm kiếm khách hàng theo số điện thoại (tìm kiếm gần đúng)")
    @GetMapping("/search/phone")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<CustomerDto>>> searchCustomersByPhone(
            @Parameter(description = "Số điện thoại khách hàng") @RequestParam String phone) {

        log.info("Nhận yêu cầu tìm kiếm khách hàng theo phone: {}", phone);

        List<CustomerDto> customers = customerService.searchCustomersByPhone(phone);

        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm khách hàng theo số điện thoại thành công", customers));
    }

    /**
     * Lấy khách hàng theo loại
     */
    @Operation(summary = "Lấy khách hàng theo loại", description = "Lấy danh sách khách hàng theo loại (REGULAR/VIP)")
    @GetMapping("/type/{customerType}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<CustomerDto>>> getCustomersByType(
            @Parameter(description = "Loại khách hàng") @PathVariable CustomerType customerType) {

        log.info("Nhận yêu cầu lấy khách hàng theo loại: {}", customerType);

        List<CustomerDto> customers = customerService.getCustomersByType(customerType);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách khách hàng theo loại thành công", customers));
    }

    /**
     * Lấy danh sách khách hàng VIP
     */
    @Operation(summary = "Lấy danh sách khách hàng VIP", description = "Lấy danh sách tất cả khách hàng VIP")
    @GetMapping("/vip")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    public ResponseEntity<ApiResponse<List<CustomerDto>>> getVipCustomers() {

        log.info("Nhận yêu cầu lấy danh sách khách hàng VIP");

        List<CustomerDto> customers = customerService.getVipCustomers();

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách khách hàng VIP thành công", customers));
    }

    /**
     * Nâng cấp khách hàng lên VIP
     */
    @Operation(summary = "Nâng cấp khách hàng lên VIP", description = "Nâng cấp một khách hàng từ REGULAR lên VIP")
    @PostMapping("/{customerId}/upgrade-to-vip")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<String>> upgradeToVip(
            @Parameter(description = "ID khách hàng") @PathVariable Integer customerId) {

        log.info("Nhận yêu cầu nâng cấp khách hàng lên VIP với ID: {}", customerId);

        customerService.upgradeToVip(customerId);

        return ResponseEntity.ok(ApiResponse.success(null, "Nâng cấp khách hàng lên VIP thành công"));
    }

    /**
     * Hạ cấp khách hàng xuống REGULAR
     */
    @Operation(summary = "Hạ cấp khách hàng xuống REGULAR", description = "Hạ cấp một khách hàng từ VIP xuống REGULAR")
    @PostMapping("/{customerId}/downgrade-to-regular")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<String>> downgradeToRegular(
            @Parameter(description = "ID khách hàng") @PathVariable Integer customerId) {

        log.info("Nhận yêu cầu hạ cấp khách hàng xuống REGULAR với ID: {}", customerId);

        customerService.downgradeToRegular(customerId);

        return ResponseEntity.ok(ApiResponse.success(null, "Hạ cấp khách hàng xuống REGULAR thành công"));
    }
}
