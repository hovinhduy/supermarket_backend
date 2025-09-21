package iuh.fit.supermarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import iuh.fit.supermarket.dto.supplier.SupplierBatchDeleteRequest;
import iuh.fit.supermarket.dto.supplier.SupplierBatchDeleteResponse;
import iuh.fit.supermarket.dto.supplier.SupplierCreateRequest;
import iuh.fit.supermarket.dto.supplier.SupplierPageableRequest;
import iuh.fit.supermarket.dto.supplier.SupplierResponse;
import iuh.fit.supermarket.dto.supplier.SupplierUpdateRequest;
import iuh.fit.supermarket.service.SupplierService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý các API liên quan đến quản lý nhà cung cấp
 */
@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Supplier Management", description = "APIs cho quản lý nhà cung cấp")
@SecurityRequirement(name = "Bearer Authentication")
public class SupplierController {

        private final SupplierService supplierService;

        /**
         * API tạo nhà cung cấp mới
         */
        @PostMapping
        @Operation(summary = "Tạo nhà cung cấp mới", description = "Tạo một nhà cung cấp mới trong hệ thống")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Tạo nhà cung cấp thành công"),
                        @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
                        @ApiResponse(responseCode = "409", description = "Mã nhà cung cấp đã tồn tại"),
                        @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
        })
        public ResponseEntity<iuh.fit.supermarket.dto.common.ApiResponse<SupplierResponse>> createSupplier(
                        @Valid @RequestBody SupplierCreateRequest request) {

                log.info("API tạo mới nhà cung cấp với tên: {}", request.getName());

                try {
                        SupplierResponse supplierResponse = supplierService.createSupplier(request);

                        iuh.fit.supermarket.dto.common.ApiResponse<SupplierResponse> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .success(
                                                        "Tạo nhà cung cấp thành công", supplierResponse);

                        return ResponseEntity.status(HttpStatus.CREATED).body(response);

                } catch (IllegalArgumentException e) {
                        log.error("Lỗi validation khi tạo nhà cung cấp: {}", e.getMessage());

                        iuh.fit.supermarket.dto.common.ApiResponse<SupplierResponse> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .error(e.getMessage());
                        return ResponseEntity.badRequest().body(response);

                } catch (Exception e) {
                        log.error("Lỗi không mong muốn khi tạo nhà cung cấp: ", e);

                        iuh.fit.supermarket.dto.common.ApiResponse<SupplierResponse> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .error("Có lỗi xảy ra khi tạo nhà cung cấp");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
        }

        /**
         * API cập nhật thông tin nhà cung cấp
         */
        @PutMapping("/{id}")
        @Operation(summary = "Cập nhật thông tin nhà cung cấp", description = "Cập nhật thông tin của một nhà cung cấp đã tồn tại, bao gồm cả mã code")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Cập nhật nhà cung cấp thành công"),
                        @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy nhà cung cấp"),
                        @ApiResponse(responseCode = "409", description = "Mã nhà cung cấp đã tồn tại"),
                        @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
        })
        public ResponseEntity<iuh.fit.supermarket.dto.common.ApiResponse<SupplierResponse>> updateSupplier(
                        @Parameter(description = "ID của nhà cung cấp", required = true) @PathVariable Integer id,
                        @Valid @RequestBody SupplierUpdateRequest request) {

                log.info("API cập nhật nhà cung cấp ID: {} với tên: {}, mã code: {}", id, request.getName(),
                                request.getCode());

                try {
                        SupplierResponse supplierResponse = supplierService.updateSupplier(id, request);

                        iuh.fit.supermarket.dto.common.ApiResponse<SupplierResponse> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .success(
                                                        "Cập nhật nhà cung cấp thành công", supplierResponse);

                        return ResponseEntity.ok(response);

                } catch (IllegalArgumentException e) {
                        log.error("Lỗi validation khi cập nhật nhà cung cấp: {}", e.getMessage());

                        iuh.fit.supermarket.dto.common.ApiResponse<SupplierResponse> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .error(e.getMessage());

                        // Kiểm tra loại lỗi để trả về status code phù hợp
                        if (e.getMessage().contains("không tìm thấy") || e.getMessage().contains("not found")) {
                                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                        } else if (e.getMessage().contains("đã tồn tại") || e.getMessage().contains("already exists")) {
                                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
                        } else {
                                return ResponseEntity.badRequest().body(response);
                        }

                } catch (Exception e) {
                        log.error("Lỗi không mong muốn khi cập nhật nhà cung cấp: ", e);

                        iuh.fit.supermarket.dto.common.ApiResponse<SupplierResponse> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .error("Có lỗi xảy ra khi cập nhật nhà cung cấp");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
        }

        /**
         * API cập nhật trạng thái hoạt động của nhà cung cấp
         */
        @PatchMapping("/{id}/status")
        @Operation(summary = "Cập nhật trạng thái hoạt động nhà cung cấp", description = "Cập nhật chỉ trạng thái hoạt động (isActive) của nhà cung cấp")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Cập nhật trạng thái thành công"),
                        @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy nhà cung cấp"),
                        @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
        })
        public ResponseEntity<iuh.fit.supermarket.dto.common.ApiResponse<SupplierResponse>> updateSupplierStatus(
                        @Parameter(description = "ID của nhà cung cấp", required = true) @PathVariable Integer id,
                        @Valid @RequestBody iuh.fit.supermarket.dto.supplier.SupplierStatusUpdateRequest request) {

                log.info("API cập nhật trạng thái hoạt động nhà cung cấp ID: {} thành: {}", id, request.getIsActive());

                try {
                        SupplierResponse supplierResponse = supplierService.updateSupplierStatus(id,
                                        request.getIsActive());

                        iuh.fit.supermarket.dto.common.ApiResponse<SupplierResponse> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .success("Cập nhật trạng thái nhà cung cấp thành công", supplierResponse);

                        return ResponseEntity.ok(response);

                } catch (IllegalArgumentException e) {
                        log.error("Lỗi validation khi cập nhật trạng thái nhà cung cấp: {}", e.getMessage());

                        iuh.fit.supermarket.dto.common.ApiResponse<SupplierResponse> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .error(e.getMessage());

                        // Kiểm tra loại lỗi để trả về status code phù hợp
                        if (e.getMessage().contains("không tìm thấy") || e.getMessage().contains("not found")) {
                                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                        } else {
                                return ResponseEntity.badRequest().body(response);
                        }

                } catch (Exception e) {
                        log.error("Lỗi không mong muốn khi cập nhật trạng thái nhà cung cấp: ", e);

                        iuh.fit.supermarket.dto.common.ApiResponse<SupplierResponse> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .error("Có lỗi xảy ra khi cập nhật trạng thái nhà cung cấp");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
        }

        /**
         * API xóa nhà cung cấp (soft delete)
         */
        @DeleteMapping("/{id}")
        @Operation(summary = "Xóa nhà cung cấp", description = "Xóa mềm một nhà cung cấp (đánh dấu isDeleted = true)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Xóa nhà cung cấp thành công"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy nhà cung cấp"),
                        @ApiResponse(responseCode = "409", description = "Không thể xóa do có ràng buộc dữ liệu"),
                        @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
        })
        public ResponseEntity<iuh.fit.supermarket.dto.common.ApiResponse<Void>> deleteSupplier(
                        @Parameter(description = "ID của nhà cung cấp", required = true) @PathVariable Integer id) {

                log.info("API xóa nhà cung cấp ID: {}", id);

                try {
                        supplierService.deleteSupplier(id);

                        iuh.fit.supermarket.dto.common.ApiResponse<Void> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .success(
                                                        "Xóa nhà cung cấp thành công", null);

                        return ResponseEntity.ok(response);

                } catch (IllegalArgumentException e) {
                        log.error("Không tìm thấy nhà cung cấp: {}", e.getMessage());

                        iuh.fit.supermarket.dto.common.ApiResponse<Void> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .error(e.getMessage());
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

                } catch (IllegalStateException e) {
                        log.error("Không thể xóa nhà cung cấp: {}", e.getMessage());

                        iuh.fit.supermarket.dto.common.ApiResponse<Void> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .error(e.getMessage());
                        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

                } catch (Exception e) {
                        log.error("Lỗi không mong muốn khi xóa nhà cung cấp: ", e);

                        iuh.fit.supermarket.dto.common.ApiResponse<Void> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .error("Có lỗi xảy ra khi xóa nhà cung cấp");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
        }

        /**
         * API lấy thông tin nhà cung cấp theo ID
         */
        @GetMapping("/{id}")
        @Operation(summary = "Lấy thông tin nhà cung cấp theo ID", description = "Lấy thông tin chi tiết của một nhà cung cấp")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lấy thông tin nhà cung cấp thành công"),
                        @ApiResponse(responseCode = "404", description = "Không tìm thấy nhà cung cấp"),
                        @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
        })
        public ResponseEntity<iuh.fit.supermarket.dto.common.ApiResponse<SupplierResponse>> getSupplierById(
                        @Parameter(description = "ID của nhà cung cấp", required = true) @PathVariable Integer id) {

                log.info("API lấy thông tin nhà cung cấp ID: {}", id);

                try {
                        SupplierResponse supplierResponse = supplierService.getSupplierById(id);

                        iuh.fit.supermarket.dto.common.ApiResponse<SupplierResponse> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .success(
                                                        "Lấy thông tin nhà cung cấp thành công", supplierResponse);

                        return ResponseEntity.ok(response);

                } catch (IllegalArgumentException e) {
                        log.error("Không tìm thấy nhà cung cấp: {}", e.getMessage());

                        iuh.fit.supermarket.dto.common.ApiResponse<SupplierResponse> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .error(e.getMessage());
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

                } catch (Exception e) {
                        log.error("Lỗi không mong muốn khi lấy thông tin nhà cung cấp: ", e);

                        iuh.fit.supermarket.dto.common.ApiResponse<SupplierResponse> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .error("Có lỗi xảy ra khi lấy thông tin nhà cung cấp");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
        }

        /**
         * API lấy danh sách nhà cung cấp với phân trang nâng cao
         */
        @PostMapping("/list")
        @Operation(summary = "Lấy danh sách nhà cung cấp với phân trang nâng cao", description = "Lấy danh sách nhà cung cấp có hỗ trợ phân trang, sắp xếp, tìm kiếm và filtering nâng cao")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lấy danh sách nhà cung cấp thành công"),
                        @ApiResponse(responseCode = "400", description = "Dữ liệu request không hợp lệ"),
                        @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
        })
        public ResponseEntity<iuh.fit.supermarket.dto.common.ApiResponse<Page<SupplierResponse>>> getSuppliersAdvanced(
                        @Valid @RequestBody SupplierPageableRequest request) {

                log.info("API lấy danh sách nhà cung cấp nâng cao - page: {}, limit: {}, search: {}, isActive: {}",
                                request.getPage(), request.getLimit(), request.getSearchTerm(), request.getIsActive());

                try {
                        Page<SupplierResponse> supplierPage = supplierService.getSuppliersAdvanced(request);

                        iuh.fit.supermarket.dto.common.ApiResponse<Page<SupplierResponse>> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .success(
                                                        "Lấy danh sách nhà cung cấp thành công", supplierPage);

                        return ResponseEntity.ok(response);

                } catch (IllegalArgumentException e) {
                        log.error("Lỗi validation request: {}", e.getMessage());

                        iuh.fit.supermarket.dto.common.ApiResponse<Page<SupplierResponse>> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .error(e.getMessage());
                        return ResponseEntity.badRequest().body(response);

                } catch (Exception e) {
                        log.error("Lỗi không mong muốn khi lấy danh sách nhà cung cấp: ", e);

                        iuh.fit.supermarket.dto.common.ApiResponse<Page<SupplierResponse>> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .error("Có lỗi xảy ra khi lấy danh sách nhà cung cấp");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
        }

        /**
         * API lấy danh sách nhà cung cấp đang hoạt động
         */
        @GetMapping("/active")
        @Operation(summary = "Lấy danh sách nhà cung cấp đang hoạt động", description = "Lấy danh sách tất cả nhà cung cấp có isActive = true và isDeleted = false")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Lấy danh sách nhà cung cấp thành công"),
                        @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
        })
        public ResponseEntity<iuh.fit.supermarket.dto.common.ApiResponse<List<SupplierResponse>>> getAllActiveSuppliers() {

                log.info("API lấy danh sách tất cả nhà cung cấp đang hoạt động");

                try {
                        List<SupplierResponse> suppliers = supplierService.getAllActiveSuppliers();

                        iuh.fit.supermarket.dto.common.ApiResponse<List<SupplierResponse>> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .success(
                                                        "Lấy danh sách nhà cung cấp thành công", suppliers);

                        return ResponseEntity.ok(response);

                } catch (Exception e) {
                        log.error("Lỗi không mong muốn khi lấy danh sách nhà cung cấp: ", e);

                        iuh.fit.supermarket.dto.common.ApiResponse<List<SupplierResponse>> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .error(
                                                        "Có lỗi xảy ra khi lấy danh sách nhà cung cấp");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
        }

        /**
         * API tìm kiếm nhà cung cấp
         */
        @GetMapping("/search")
        @Operation(summary = "Tìm kiếm nhà cung cấp", description = "Tìm kiếm nhà cung cấp theo từ khóa (tên, email, phone, code) với hỗ trợ phân trang")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Tìm kiếm nhà cung cấp thành công"),
                        @ApiResponse(responseCode = "400", description = "Từ khóa tìm kiếm không hợp lệ"),
                        @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
        })
        public ResponseEntity<iuh.fit.supermarket.dto.common.ApiResponse<Page<SupplierResponse>>> searchSuppliersWithPaging(
                        @Parameter(description = "Từ khóa tìm kiếm", required = true) @RequestParam String keyword,
                        @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0") @RequestParam(defaultValue = "0") int page,
                        @Parameter(description = "Số lượng bản ghi trên mỗi trang", example = "10") @RequestParam(defaultValue = "10") int size,
                        @Parameter(description = "Trường sắp xếp", example = "name") @RequestParam(defaultValue = "name") String sortBy,
                        @Parameter(description = "Hướng sắp xếp (asc/desc)", example = "asc") @RequestParam(defaultValue = "asc") String sortDir) {

                log.info("API tìm kiếm nhà cung cấp với từ khóa: {} - page: {}, size: {}, sortBy: {}, sortDir: {}",
                                keyword, page, size, sortBy, sortDir);

                try {
                        if (keyword == null || keyword.trim().isEmpty()) {
                                throw new IllegalArgumentException("Từ khóa tìm kiếm không được để trống");
                        }

                        if (page < 0) {
                                throw new IllegalArgumentException("Số trang không được nhỏ hơn 0");
                        }
                        if (size <= 0 || size > 100) {
                                throw new IllegalArgumentException("Kích thước trang phải từ 1 đến 100");
                        }

                        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC
                                        : Sort.Direction.ASC;
                        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
                        Page<SupplierResponse> supplierPage = supplierService.searchSuppliers(keyword, pageable);

                        iuh.fit.supermarket.dto.common.ApiResponse<Page<SupplierResponse>> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .success(
                                                        "Tìm kiếm nhà cung cấp thành công", supplierPage);

                        return ResponseEntity.ok(response);

                } catch (IllegalArgumentException e) {
                        log.error("Lỗi validation khi tìm kiếm nhà cung cấp: {}", e.getMessage());

                        iuh.fit.supermarket.dto.common.ApiResponse<Page<SupplierResponse>> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .error(e.getMessage());
                        return ResponseEntity.badRequest().body(response);

                } catch (Exception e) {
                        log.error("Lỗi không mong muốn khi tìm kiếm nhà cung cấp: ", e);

                        iuh.fit.supermarket.dto.common.ApiResponse<Page<SupplierResponse>> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .error("Có lỗi xảy ra khi tìm kiếm nhà cung cấp");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
        }

        /**
         * API xóa nhiều nhà cung cấp cùng lúc
         */
        @DeleteMapping("/delete")
        @Operation(summary = "Xóa nhiều nhà cung cấp cùng lúc", description = "Xóa mềm nhiều nhà cung cấp cùng lúc theo danh sách ID (đánh dấu isDeleted = true)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Xóa nhà cung cấp thành công"),
                        @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
                        @ApiResponse(responseCode = "500", description = "Lỗi hệ thống")
        })
        public ResponseEntity<iuh.fit.supermarket.dto.common.ApiResponse<SupplierBatchDeleteResponse>> batchDeleteSuppliers(
                        @Valid @RequestBody SupplierBatchDeleteRequest request) {

                log.info("API xóa batch {} nhà cung cấp: {}", request.getCount(), request.getSupplierIds());

                try {
                        SupplierBatchDeleteResponse result = supplierService.batchDeleteSuppliers(request);

                        // Tạo message phù hợp
                        String message = result.getResultMessage();

                        iuh.fit.supermarket.dto.common.ApiResponse<SupplierBatchDeleteResponse> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .success(message, result);

                        return ResponseEntity.ok(response);

                } catch (IllegalArgumentException e) {
                        log.error("Lỗi validation khi xóa batch nhà cung cấp: {}", e.getMessage());

                        iuh.fit.supermarket.dto.common.ApiResponse<SupplierBatchDeleteResponse> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .error(e.getMessage());
                        return ResponseEntity.badRequest().body(response);

                } catch (Exception e) {
                        log.error("Lỗi không mong muốn khi xóa batch nhà cung cấp: ", e);

                        iuh.fit.supermarket.dto.common.ApiResponse<SupplierBatchDeleteResponse> response = iuh.fit.supermarket.dto.common.ApiResponse
                                        .error("Có lỗi xảy ra khi xóa nhà cung cấp");
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
                }
        }
}
