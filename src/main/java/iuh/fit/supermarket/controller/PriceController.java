package iuh.fit.supermarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.dto.price.*;
import iuh.fit.supermarket.enums.PriceType;
import iuh.fit.supermarket.exception.*;
import iuh.fit.supermarket.service.PriceService;
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
 * Controller cho quản lý bảng giá
 * Cung cấp các REST API endpoints cho CRUD operations và các chức năng đặc biệt
 */
@RestController
@RequestMapping("/api/prices")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Price Management", description = "API quản lý bảng giá")
public class PriceController {

    private final PriceService priceService;

    /**
     * Tạo bảng giá mới
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Tạo bảng giá mới", description = "Tạo bảng giá mới với thông tin chi tiết")
    public ResponseEntity<ApiResponse<PriceResponse>> createPrice(
            @Valid @RequestBody PriceCreateRequest request) {

        log.info("API tạo bảng giá mới: {}", request.getPriceCode());

        try {
            PriceResponse priceResponse = priceService.createPrice(request);

            ApiResponse<PriceResponse> response = ApiResponse.success(
                    "Tạo bảng giá thành công", priceResponse);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (DuplicatePriceCodeException e) {
            log.error("Mã bảng giá đã tồn tại: ", e);
            ApiResponse<PriceResponse> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (PriceValidationException e) {
            log.error("Dữ liệu bảng giá không hợp lệ: ", e);
            ApiResponse<PriceResponse> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Lỗi khi tạo bảng giá: ", e);
            ApiResponse<PriceResponse> response = ApiResponse.error("Lỗi hệ thống khi tạo bảng giá");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Cập nhật bảng giá
     */
    @PutMapping("/{priceId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Cập nhật bảng giá", description = "Cập nhật thông tin bảng giá theo ID")
    public ResponseEntity<ApiResponse<PriceResponse>> updatePrice(
            @Parameter(description = "ID bảng giá") @PathVariable Long priceId,
            @Valid @RequestBody PriceUpdateRequest request) {

        log.info("API cập nhật bảng giá ID: {}", priceId);

        try {
            PriceResponse priceResponse = priceService.updatePrice(priceId, request);

            ApiResponse<PriceResponse> response = ApiResponse.success(
                    "Cập nhật bảng giá thành công", priceResponse);

            return ResponseEntity.ok(response);

        } catch (PriceNotFoundException e) {
            log.error("Không tìm thấy bảng giá: ", e);
            return ResponseEntity.notFound().build();
        } catch (PriceConflictException e) {
            log.error("Xung đột khi cập nhật bảng giá: ", e);
            ApiResponse<PriceResponse> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (PriceValidationException e) {
            log.error("Dữ liệu bảng giá không hợp lệ: ", e);
            ApiResponse<PriceResponse> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật bảng giá: ", e);
            ApiResponse<PriceResponse> response = ApiResponse.error("Lỗi hệ thống khi cập nhật bảng giá");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy thông tin bảng giá theo ID
     */
    @GetMapping("/{priceId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    @Operation(summary = "Lấy thông tin bảng giá", description = "Lấy thông tin chi tiết bảng giá theo ID")
    public ResponseEntity<ApiResponse<PriceResponse>> getPriceById(
            @Parameter(description = "ID bảng giá") @PathVariable Long priceId,
            @Parameter(description = "Có bao gồm chi tiết giá không") @RequestParam(defaultValue = "false") boolean includeDetails) {

        log.info("API lấy thông tin bảng giá ID: {}, includeDetails: {}", priceId, includeDetails);

        try {
            PriceResponse priceResponse = priceService.getPriceById(priceId, includeDetails);

            ApiResponse<PriceResponse> response = ApiResponse.success(priceResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin bảng giá: ", e);

            ApiResponse<PriceResponse> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy thông tin bảng giá theo mã
     */
    @GetMapping("/code/{priceCode}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    @Operation(summary = "Lấy thông tin bảng giá theo mã", description = "Lấy thông tin chi tiết bảng giá theo mã")
    public ResponseEntity<ApiResponse<PriceResponse>> getPriceByCode(
            @Parameter(description = "Mã bảng giá") @PathVariable String priceCode,
            @Parameter(description = "Có bao gồm chi tiết giá không") @RequestParam(defaultValue = "false") boolean includeDetails) {

        log.info("API lấy thông tin bảng giá mã: {}, includeDetails: {}", priceCode, includeDetails);

        try {
            PriceResponse priceResponse = priceService.getPriceByCode(priceCode, includeDetails);

            ApiResponse<PriceResponse> response = ApiResponse.success(priceResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin bảng giá: ", e);

            ApiResponse<PriceResponse> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy danh sách bảng giá với phân trang và lọc
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    @Operation(summary = "Tìm kiếm bảng giá nâng cao", description = "Lấy danh sách bảng giá với phân trang, lọc và sắp xếp")
    public ResponseEntity<ApiResponse<Page<PriceResponse>>> getPricesAdvanced(
            @Valid @RequestBody PricePageableRequest request) {

        log.info("API tìm kiếm bảng giá nâng cao: page={}, limit={}, search={}, status={}",
                request.getPage(), request.getLimit(), request.getSearchTerm(), request.getStatus());

        try {
            Page<PriceResponse> pricePage = priceService.getPricesAdvanced(request);

            ApiResponse<Page<PriceResponse>> response = ApiResponse.success(
                    "Lấy danh sách bảng giá thành công", pricePage);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm bảng giá: ", e);

            ApiResponse<Page<PriceResponse>> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Xóa bảng giá
     */
    @DeleteMapping("/{priceId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Xóa bảng giá", description = "Xóa bảng giá theo ID (chỉ cho phép xóa bảng giá UPCOMING hoặc PAUSED)")
    public ResponseEntity<ApiResponse<String>> deletePrice(
            @Parameter(description = "ID bảng giá") @PathVariable Long priceId) {

        log.info("API xóa bảng giá ID: {}", priceId);

        try {
            priceService.deletePrice(priceId);

            ApiResponse<String> response = ApiResponse.success("Xóa bảng giá thành công", null);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi xóa bảng giá: ", e);

            ApiResponse<String> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Cập nhật trạng thái bảng giá
     */
    @PatchMapping("/{priceId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Cập nhật trạng thái bảng giá", description = "Cập nhật trạng thái bảng giá theo ID")
    public ResponseEntity<ApiResponse<PriceResponse>> updatePriceStatus(
            @Parameter(description = "ID bảng giá") @PathVariable Long priceId,
            @Valid @RequestBody PriceStatusUpdateRequest request) {

        log.info("API cập nhật trạng thái bảng giá ID: {} sang {}", priceId, request.getStatus());

        try {
            PriceResponse priceResponse = priceService.updatePriceStatus(priceId, request);

            ApiResponse<PriceResponse> response = ApiResponse.success(
                    "Cập nhật trạng thái bảng giá thành công", priceResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi cập nhật trạng thái bảng giá: ", e);

            ApiResponse<PriceResponse> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Kích hoạt bảng giá
     */
    @PostMapping("/{priceId}/activate")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Kích hoạt bảng giá", description = "Chuyển bảng giá từ UPCOMING/PAUSED sang CURRENT")
    public ResponseEntity<ApiResponse<PriceResponse>> activatePrice(
            @Parameter(description = "ID bảng giá") @PathVariable Long priceId) {

        log.info("API kích hoạt bảng giá ID: {}", priceId);

        try {
            PriceResponse priceResponse = priceService.activatePrice(priceId);

            ApiResponse<PriceResponse> response = ApiResponse.success(
                    "Kích hoạt bảng giá thành công", priceResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi kích hoạt bảng giá: ", e);

            ApiResponse<PriceResponse> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Tạm dừng bảng giá
     */
    @PostMapping("/{priceId}/pause")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Tạm dừng bảng giá", description = "Chuyển bảng giá từ CURRENT sang PAUSED")
    public ResponseEntity<ApiResponse<PriceResponse>> pausePrice(
            @Parameter(description = "ID bảng giá") @PathVariable Long priceId) {

        log.info("API tạm dừng bảng giá ID: {}", priceId);

        try {
            PriceResponse priceResponse = priceService.pausePrice(priceId);

            ApiResponse<PriceResponse> response = ApiResponse.success(
                    "Tạm dừng bảng giá thành công", priceResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi tạm dừng bảng giá: ", e);

            ApiResponse<PriceResponse> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy danh sách bảng giá theo trạng thái
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    @Operation(summary = "Lấy bảng giá theo trạng thái", description = "Lấy danh sách bảng giá theo trạng thái cụ thể")
    public ResponseEntity<ApiResponse<List<PriceResponse>>> getPricesByStatus(
            @Parameter(description = "Trạng thái bảng giá") @PathVariable PriceType status) {

        log.info("API lấy bảng giá theo trạng thái: {}", status);

        try {
            List<PriceResponse> prices = priceService.getPricesByStatus(status);

            ApiResponse<List<PriceResponse>> response = ApiResponse.success(
                    "Lấy danh sách bảng giá thành công", prices);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy bảng giá theo trạng thái: ", e);

            ApiResponse<List<PriceResponse>> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy bảng giá hiện tại đang áp dụng
     */
    @GetMapping("/current")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    @Operation(summary = "Lấy bảng giá hiện tại", description = "Lấy danh sách bảng giá đang áp dụng (CURRENT)")
    public ResponseEntity<ApiResponse<List<PriceResponse>>> getCurrentPrices() {

        log.info("API lấy bảng giá hiện tại");

        try {
            List<PriceResponse> prices = priceService.getCurrentPrices();

            ApiResponse<List<PriceResponse>> response = ApiResponse.success(
                    "Lấy danh sách bảng giá hiện tại thành công", prices);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy bảng giá hiện tại: ", e);

            ApiResponse<List<PriceResponse>> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy giá hiện tại của đơn vị sản phẩm
     */
    @GetMapping("/product-unit/{productUnitId}/current-price")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    @Operation(summary = "Lấy giá hiện tại của đơn vị sản phẩm", description = "Lấy giá hiện tại đang áp dụng của đơn vị sản phẩm")
    public ResponseEntity<ApiResponse<PriceDetailDto>> getCurrentPriceByVariantId(
            @Parameter(description = "ID đơn vị sản phẩm") @PathVariable Long productUnitId) {

        log.info("API lấy giá hiện tại của đơn vị sản phẩm ID: {}", productUnitId);

        try {
            PriceDetailDto priceDetail = priceService.getCurrentPriceByVariantId(productUnitId);

            if (priceDetail != null) {
                ApiResponse<PriceDetailDto> response = ApiResponse.success(
                        "Lấy giá hiện tại thành công", priceDetail);
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<PriceDetailDto> response = ApiResponse.success(
                        "Không tìm thấy giá hiện tại cho đơn vị sản phẩm này", null);
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            log.error("Lỗi khi lấy giá hiện tại của đơn vị sản phẩm: ", e);

            ApiResponse<PriceDetailDto> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy danh sách chi tiết giá theo ID bảng giá
     */
    @GetMapping("/{priceId}/details")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('EMPLOYEE')")
    @Operation(summary = "Lấy chi tiết giá", description = "Lấy danh sách chi tiết giá theo ID bảng giá")
    public ResponseEntity<ApiResponse<List<PriceDetailDto>>> getPriceDetailsByPriceId(
            @Parameter(description = "ID bảng giá") @PathVariable Long priceId) {

        log.info("API lấy chi tiết giá của bảng giá ID: {}", priceId);

        try {
            List<PriceDetailDto> priceDetails = priceService.getPriceDetailsByPriceId(priceId);

            ApiResponse<List<PriceDetailDto>> response = ApiResponse.success(
                    "Lấy danh sách chi tiết giá thành công", priceDetails);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy chi tiết giá: ", e);

            ApiResponse<List<PriceDetailDto>> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Thêm chi tiết giá vào bảng giá
     */
    @PostMapping("/{priceId}/details")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Thêm chi tiết giá", description = "Thêm danh sách chi tiết giá vào bảng giá")
    public ResponseEntity<ApiResponse<PriceResponse>> addPriceDetails(
            @Parameter(description = "ID bảng giá") @PathVariable Long priceId,
            @Valid @RequestBody List<PriceCreateRequest.PriceDetailCreateRequest> priceDetails) {

        log.info("API thêm {} chi tiết giá vào bảng giá ID: {}", priceDetails.size(), priceId);

        try {
            PriceResponse priceResponse = priceService.addPriceDetails(priceId, priceDetails);

            ApiResponse<PriceResponse> response = ApiResponse.success(
                    "Thêm chi tiết giá thành công", priceResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi thêm chi tiết giá: ", e);

            ApiResponse<PriceResponse> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Xóa chi tiết giá khỏi bảng giá
     */
    @DeleteMapping("/{priceId}/details")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @Operation(summary = "Xóa chi tiết giá", description = "Xóa danh sách chi tiết giá khỏi bảng giá")
    public ResponseEntity<ApiResponse<PriceResponse>> removePriceDetails(
            @Parameter(description = "ID bảng giá") @PathVariable Long priceId,
            @RequestBody List<Long> priceDetailIds) {

        log.info("API xóa {} chi tiết giá khỏi bảng giá ID: {}", priceDetailIds.size(), priceId);

        try {
            PriceResponse priceResponse = priceService.removePriceDetails(priceId, priceDetailIds);

            ApiResponse<PriceResponse> response = ApiResponse.success(
                    "Xóa chi tiết giá thành công", priceResponse);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi xóa chi tiết giá: ", e);

            ApiResponse<PriceResponse> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
