package iuh.fit.supermarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.dto.imports.ImportCreateRequest;
import iuh.fit.supermarket.dto.imports.ImportResponse;
import iuh.fit.supermarket.service.ImportService;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller xử lý các API liên quan đến nhập hàng
 */
@RestController
@RequestMapping("/api/imports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Import Management", description = "APIs quản lý phiếu nhập hàng")
@SecurityRequirement(name = "Bearer Authentication")
public class ImportController {

    private final ImportService importService;

    /**
     * Tạo phiếu nhập hàng mới
     */
    @PostMapping
    @Operation(summary = "Tạo phiếu nhập hàng mới", description = "Tạo phiếu nhập hàng mới với danh sách sản phẩm. Hệ thống sẽ tự động cập nhật tồn kho và ghi nhận giao dịch.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo phiếu nhập thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa xác thực"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
    })
    public ResponseEntity<ApiResponse<ImportResponse>> createImport(
            @Valid @RequestBody ImportCreateRequest request) {

        log.info("API tạo phiếu nhập hàng được gọi cho nhà cung cấp ID: {}", request.getSupplierId());

        try {
            // Lấy thông tin nhân viên từ Security Context
            Integer employeeId = getCurrentEmployeeId();

            // Tạo phiếu nhập hàng
            ImportResponse importResponse = importService.createImport(request, employeeId);

            log.info("Tạo phiếu nhập hàng thành công với mã: {}", importResponse.getImportCode());

            ApiResponse<ImportResponse> response = ApiResponse.success(
                    "Tạo phiếu nhập hàng thành công", importResponse);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.error("Dữ liệu đầu vào không hợp lệ: {}", e.getMessage());

            ApiResponse<ImportResponse> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (IllegalStateException e) {
            log.error("Lỗi trạng thái: {}", e.getMessage());

            ApiResponse<ImportResponse> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("Lỗi khi tạo phiếu nhập hàng: ", e);

            ApiResponse<ImportResponse> response = ApiResponse
                    .error("Không thể tạo phiếu nhập hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy thông tin phiếu nhập theo ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin phiếu nhập theo ID", description = "Lấy thông tin chi tiết của một phiếu nhập hàng")
    public ResponseEntity<ApiResponse<ImportResponse>> getImportById(
            @Parameter(description = "ID của phiếu nhập", required = true) @PathVariable Integer id) {

        log.info("API lấy thông tin phiếu nhập ID: {}", id);

        try {
            ImportResponse importResponse = importService.getImportById(id);

            ApiResponse<ImportResponse> response = ApiResponse.success(
                    "Lấy thông tin phiếu nhập thành công", importResponse);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Không tìm thấy phiếu nhập: {}", e.getMessage());

            ApiResponse<ImportResponse> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin phiếu nhập: ", e);

            ApiResponse<ImportResponse> response = ApiResponse
                    .error("Không thể lấy thông tin phiếu nhập: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy danh sách tất cả phiếu nhập với phân trang
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách phiếu nhập", description = "Lấy danh sách tất cả phiếu nhập hàng với phân trang và sắp xếp")
    public ResponseEntity<ApiResponse<Page<ImportResponse>>> getAllImports(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "importDate") String sortBy,
            @Parameter(description = "Hướng sắp xếp (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("API lấy danh sách phiếu nhập: page={}, size={}, sortBy={}, sortDir={}",
                page, size, sortBy, sortDir);

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<ImportResponse> imports = importService.getAllImports(pageable);

            ApiResponse<Page<ImportResponse>> response = ApiResponse.success(imports);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách phiếu nhập: ", e);

            ApiResponse<Page<ImportResponse>> response = ApiResponse
                    .error("Không thể lấy danh sách phiếu nhập: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy danh sách phiếu nhập theo nhà cung cấp
     */
    @GetMapping("/supplier/{supplierId}")
    @Operation(summary = "Lấy danh sách phiếu nhập theo nhà cung cấp", description = "Lấy danh sách phiếu nhập hàng của một nhà cung cấp cụ thể")
    public ResponseEntity<ApiResponse<Page<ImportResponse>>> getImportsBySupplier(
            @Parameter(description = "ID của nhà cung cấp", required = true) @PathVariable Integer supplierId,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "20") int size) {

        log.info("API lấy danh sách phiếu nhập theo nhà cung cấp ID: {}", supplierId);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("importDate").descending());
            Page<ImportResponse> imports = importService.getImportsBySupplier(supplierId, pageable);

            ApiResponse<Page<ImportResponse>> response = ApiResponse.success(imports);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách phiếu nhập theo nhà cung cấp: ", e);

            ApiResponse<Page<ImportResponse>> response = ApiResponse
                    .error("Không thể lấy danh sách phiếu nhập: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Tìm kiếm phiếu nhập theo từ khóa
     */
    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm phiếu nhập", description = "Tìm kiếm phiếu nhập theo mã phiếu, tên nhà cung cấp hoặc ghi chú")
    public ResponseEntity<ApiResponse<Page<ImportResponse>>> searchImports(
            @Parameter(description = "Từ khóa tìm kiếm", required = true) @RequestParam String keyword,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "20") int size) {

        log.info("API tìm kiếm phiếu nhập với từ khóa: {}", keyword);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("importDate").descending());
            Page<ImportResponse> imports = importService.searchImports(keyword, pageable);

            ApiResponse<Page<ImportResponse>> response = ApiResponse.success(imports);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm phiếu nhập: ", e);

            ApiResponse<Page<ImportResponse>> response = ApiResponse
                    .error("Không thể tìm kiếm phiếu nhập: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy danh sách phiếu nhập theo khoảng thời gian
     */
    @GetMapping("/date-range")
    @Operation(summary = "Lấy phiếu nhập theo khoảng thời gian", description = "Lấy danh sách phiếu nhập trong khoảng thời gian cụ thể")
    public ResponseEntity<ApiResponse<Page<ImportResponse>>> getImportsByDateRange(
            @Parameter(description = "Ngày bắt đầu (yyyy-MM-dd'T'HH:mm:ss)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Ngày kết thúc (yyyy-MM-dd'T'HH:mm:ss)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "20") int size) {

        log.info("API lấy phiếu nhập từ {} đến {}", startDate, endDate);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("importDate").descending());
            Page<ImportResponse> imports = importService.getImportsByDateRange(startDate, endDate, pageable);

            ApiResponse<Page<ImportResponse>> response = ApiResponse.success(imports);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy phiếu nhập theo khoảng thời gian: ", e);

            ApiResponse<Page<ImportResponse>> response = ApiResponse
                    .error("Không thể lấy phiếu nhập: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy ID nhân viên hiện tại từ Security Context
     */
    private Integer getCurrentEmployeeId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails) {
            // Giả sử email được sử dụng làm username
            String email = authentication.getName();
            // TODO: Implement logic để lấy employeeId từ email
            // Tạm thời return 1 cho demo
            return 1;
        }
        throw new IllegalStateException("Không thể xác định nhân viên hiện tại");
    }
}
