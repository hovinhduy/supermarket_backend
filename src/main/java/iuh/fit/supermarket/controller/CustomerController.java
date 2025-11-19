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
import org.springframework.web.multipart.MultipartFile;
import iuh.fit.supermarket.service.CustomerExcelService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

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
        private final CustomerExcelService customerExcelService;

        /**
         * Lấy danh sách khách hàng với phân trang
         */
        @Operation(summary = "Lấy danh sách khách hàng với phân trang", description = "Lấy danh sách khách hàng với phân trang và sắp xếp")
        @GetMapping("/paginated")
        @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
        public ResponseEntity<ApiResponse<Page<CustomerDto>>> getCustomersWithPagination(
                        @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Trường sắp xếp (name, email, phone, createdAt, updatedAt, customerType, address, customerCode)") @RequestParam(defaultValue = "createdAt") String sortBy,
                        @Parameter(description = "Hướng sắp xếp (asc, desc)") @RequestParam(defaultValue = "desc") String sortDirection) {

                log.info("Nhận yêu cầu lấy danh sách khách hàng với phân trang: page={}, size={}, sortBy={}, sortDirection={}",
                                page, size, sortBy, sortDirection);

                Page<CustomerDto> customers = customerService.getCustomersWithPagination(page, size, sortBy,
                                sortDirection);

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
         * Xóa nhiều khách hàng cùng lúc (bulk delete)
         */
        @Operation(summary = "Xóa nhiều khách hàng cùng lúc", description = "Xóa mềm nhiều khách hàng dựa trên danh sách ID. Trả về thống kê chi tiết về kết quả xóa.")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa thành công (có thể một phần)", content = @Content(schema = @Schema(implementation = BulkDeleteCustomersResponse.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập - Chỉ ADMIN và MANAGER")
        })
        @DeleteMapping("/bulk-delete")
        @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
        public ResponseEntity<ApiResponse<BulkDeleteCustomersResponse>> bulkDeleteCustomers(
                        @Parameter(description = "Danh sách ID khách hàng cần xóa", required = true) @Valid @RequestBody BulkDeleteCustomersRequest request) {

                log.info("Nhận yêu cầu xóa nhiều khách hàng - Số lượng: {}, IDs: {}",
                                request.getIdsCount(), request.getCustomerIds());

                BulkDeleteCustomersResponse result = customerService.bulkDeleteCustomers(request);

                // Log kết quả chi tiết cho audit
                log.info("Hoàn thành xóa nhiều khách hàng - Tổng: {}, Thành công: {}, Thất bại: {}",
                                result.getTotalRequested(), result.getSuccessCount(), result.getFailedCount());

                if (result.hasErrors()) {
                        log.warn("Có lỗi khi xóa khách hàng - IDs thất bại: {}, Lỗi: {}",
                                        result.getFailedIds(), result.getErrors());
                }

                // Tạo message phù hợp
                String message;
                if (result.isAllSuccess()) {
                        message = "Xóa tất cả khách hàng thành công";
                } else if (result.getSuccessCount() > 0) {
                        message = String.format("Xóa một phần thành công - %d/%d khách hàng",
                                        result.getSuccessCount(), result.getTotalRequested());
                } else {
                        message = "Không thể xóa khách hàng nào";
                }

                return ResponseEntity.ok(ApiResponse.success(message, result));
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
         * Tìm kiếm khách hàng nâng cao với nhiều tiêu chí tùy chọn
         */
        @Operation(summary = "Tìm kiếm khách hàng nâng cao", description = "API tìm kiếm khách hàng với nhiều tiêu chí: tên, email, số điện thoại, giới tính, loại khách hàng và hỗ trợ phân trang")
        @ApiResponses(value = {
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tìm kiếm thành công", content = @Content(schema = @Schema(implementation = CustomerDto.class))),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
        })
        @PostMapping("/list")
        @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
        public ResponseEntity<ApiResponse<Page<CustomerDto>>> searchCustomersAdvanced(
                        @Parameter(description = "Thông tin tìm kiếm nâng cao", required = true) @Valid @RequestBody CustomerAdvancedSearchRequest request) {

                log.info(
                                "Nhận yêu cầu tìm kiếm khách hàng nâng cao - từ khóa: '{}', giới tính: {}, loại: {}, trang: {}, limit: {}",
                                request.getSearchTerm(), request.getGender(), request.getCustomerType(),
                                request.getPage(), request.getLimit());

                Page<CustomerDto> customers = customerService.searchCustomersAdvanced(request);

                log.info("Tìm kiếm khách hàng nâng cao hoàn thành - tìm thấy {} khách hàng trên tổng số {} khách hàng",
                                customers.getNumberOfElements(), customers.getTotalElements());

                return ResponseEntity.ok(ApiResponse.success("Tìm kiếm khách hàng nâng cao thành công", customers));
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

        // ==================== EXCEL IMPORT/EXPORT ====================

        /**
         * API export danh sách khách hàng ra file Excel
         */
        @GetMapping("/export")
        @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
        @Operation(summary = "Export khách hàng ra Excel", description = "Export danh sách khách hàng ra file Excel")
        public ResponseEntity<byte[]> exportCustomersToExcel() {
                log.info("API export danh sách khách hàng ra Excel");

                try {
                        // Lấy tất cả khách hàng
                        List<CustomerDto> customers = customerService.getAllCustomers();

                        // Export ra Excel
                        byte[] excelData = customerExcelService.exportCustomersToExcel(customers);

                        // Tạo tên file với timestamp
                        String fileName = "danh_sach_khach_hang_" +
                                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                                        + ".xlsx";

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                        headers.setContentDispositionFormData("attachment", fileName);
                        headers.setContentLength(excelData.length);

                        log.info("Export Excel thành công với {} khách hàng", customers.size());
                        return ResponseEntity.ok()
                                        .headers(headers)
                                        .body(excelData);

                } catch (IOException e) {
                        log.error("Lỗi khi export Excel: ", e);
                        return ResponseEntity.internalServerError().build();
                } catch (Exception e) {
                        log.error("Lỗi khi export khách hàng: ", e);
                        return ResponseEntity.internalServerError().build();
                }
        }

        /**
         * API import khách hàng từ file Excel
         */
        @PostMapping("/import")
        @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
        @Operation(summary = "Import khách hàng từ Excel", description = "Import danh sách khách hàng từ file Excel")
        public ResponseEntity<ApiResponse<String>> importCustomersFromExcel(
                        @Parameter(description = "File Excel chứa danh sách khách hàng") @RequestParam("file") MultipartFile file) {
                log.info("API import khách hàng từ file Excel: {}", file.getOriginalFilename());

                try {
                        // Validate file
                        if (file.isEmpty()) {
                                return ResponseEntity.badRequest()
                                                .body(ApiResponse.error("File không được để trống"));
                        }

                        String fileName = file.getOriginalFilename();
                        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
                                return ResponseEntity.badRequest()
                                                .body(ApiResponse.error(
                                                                "File phải có định dạng Excel (.xlsx hoặc .xls)"));
                        }

                        // Parse Excel file
                        List<CreateCustomerRequest> requests = customerExcelService.importCustomersFromExcel(file);

                        if (requests.isEmpty()) {
                                return ResponseEntity.badRequest()
                                                .body(ApiResponse.error(
                                                                "File Excel không chứa dữ liệu khách hàng hợp lệ"));
                        }

                        // Import customers
                        int successCount = 0;
                        int errorCount = 0;
                        StringBuilder errorMessages = new StringBuilder();

                        for (int i = 0; i < requests.size(); i++) {
                                try {
                                        customerService.createCustomerByAdmin(requests.get(i));
                                        successCount++;
                                } catch (Exception e) {
                                        errorCount++;
                                        errorMessages.append("Dòng ").append(i + 2).append(": ").append(e.getMessage())
                                                        .append("; ");
                                        log.warn("Lỗi khi import khách hàng dòng {}: {}", i + 2, e.getMessage());
                                }
                        }

                        String message = String.format("Import hoàn tất: %d thành công, %d lỗi", successCount,
                                        errorCount);
                        if (errorCount > 0) {
                                message += ". Chi tiết lỗi: " + errorMessages.toString();
                        }

                        log.info("Import Excel hoàn tất: {} thành công, {} lỗi", successCount, errorCount);
                        return ResponseEntity.ok(ApiResponse.success(message));

                } catch (IOException e) {
                        log.error("Lỗi khi đọc file Excel: ", e);
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.error("Lỗi khi đọc file Excel: " + e.getMessage()));
                } catch (Exception e) {
                        log.error("Lỗi khi import khách hàng: ", e);
                        return ResponseEntity.badRequest()
                                        .body(ApiResponse.error("Lỗi khi import: " + e.getMessage()));
                }
        }

        /**
         * API tải template Excel để import khách hàng
         */
        @GetMapping("/import/template")
        @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
        @Operation(summary = "Tải template Excel", description = "Tải file template Excel để import khách hàng")
        public ResponseEntity<byte[]> downloadImportTemplate() {
                log.info("API tải template Excel import khách hàng");

                try {
                        byte[] templateData = customerExcelService.createImportTemplate();

                        String fileName = "template_import_khach_hang.xlsx";

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                        headers.setContentDispositionFormData("attachment", fileName);
                        headers.setContentLength(templateData.length);

                        log.info("Tải template Excel thành công");
                        return ResponseEntity.ok()
                                        .headers(headers)
                                        .body(templateData);

                } catch (IOException e) {
                        log.error("Lỗi khi tạo template Excel: ", e);
                        return ResponseEntity.internalServerError().build();
                } catch (Exception e) {
                        log.error("Lỗi khi tạo template: ", e);
                        return ResponseEntity.internalServerError().build();
                }
        }
}
