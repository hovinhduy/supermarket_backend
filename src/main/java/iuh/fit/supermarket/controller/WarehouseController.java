package iuh.fit.supermarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.dto.warehouse.WarehouseDto;
import iuh.fit.supermarket.dto.warehouse.WarehouseTransactionDto;
import iuh.fit.supermarket.entity.WarehouseTransaction;
import iuh.fit.supermarket.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller xử lý các API liên quan đến quản lý tồn kho
 */
@RestController
@RequestMapping("/api/warehouse")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Warehouse Management", description = "APIs quản lý tồn kho và giao dịch kho")
@SecurityRequirement(name = "Bearer Authentication")
public class WarehouseController {

    private final WarehouseService warehouseService;

    /**
     * Lấy thông tin tồn kho theo đơn vị sản phẩm
     */
    @GetMapping("/product-unit/{productUnitId}")
    @Operation(summary = "Lấy thông tin tồn kho theo đơn vị sản phẩm", description = "Lấy thông tin tồn kho hiện tại của một đơn vị sản phẩm")
    public ResponseEntity<ApiResponse<WarehouseDto>> getWarehouseByProductUnitId(
            @Parameter(description = "ID của đơn vị sản phẩm", required = true) @PathVariable Long productUnitId) {

        log.info("API lấy thông tin tồn kho cho đơn vị sản phẩm ID: {}", productUnitId);

        try {
            WarehouseDto warehouse = warehouseService.getWarehouseByProductUnitId(productUnitId);

            ApiResponse<WarehouseDto> response = ApiResponse.success(
                    "Lấy thông tin tồn kho thành công", warehouse);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Không tìm thấy tồn kho: {}", e.getMessage());

            ApiResponse<WarehouseDto> response = ApiResponse.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin tồn kho: ", e);

            ApiResponse<WarehouseDto> response = ApiResponse
                    .error("Không thể lấy thông tin tồn kho: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy số lượng tồn kho hiện tại
     */
    @GetMapping("/product-unit/{productUnitId}/stock")
    @Operation(summary = "Lấy số lượng tồn kho hiện tại", description = "Lấy số lượng tồn kho hiện tại của một đơn vị sản phẩm")
    public ResponseEntity<ApiResponse<Integer>> getCurrentStock(
            @Parameter(description = "ID của đơn vị sản phẩm", required = true) @PathVariable Long productUnitId) {

        log.info("API lấy số lượng tồn kho cho đơn vị sản phẩm ID: {}", productUnitId);

        try {
            Integer currentStock = warehouseService.getCurrentStock(productUnitId);

            ApiResponse<Integer> response = ApiResponse.success(
                    "Lấy số lượng tồn kho thành công", currentStock);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy số lượng tồn kho: ", e);

            ApiResponse<Integer> response = ApiResponse.error("Không thể lấy số lượng tồn kho: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Kiểm tra tồn kho có đủ để xuất không
     */
    @GetMapping("/product-unit/{productUnitId}/available")
    @Operation(summary = "Kiểm tra tồn kho có đủ", description = "Kiểm tra tồn kho có đủ để xuất một số lượng cụ thể không")
    public ResponseEntity<ApiResponse<Boolean>> isStockAvailable(
            @Parameter(description = "ID của đơn vị sản phẩm", required = true) @PathVariable Long productUnitId,
            @Parameter(description = "Số lượng cần kiểm tra", required = true) @RequestParam Integer requiredQuantity) {

        log.info("API kiểm tra tồn kho cho đơn vị sản phẩm ID: {}, số lượng: {}", productUnitId, requiredQuantity);

        try {
            Boolean isAvailable = warehouseService.isStockAvailable(productUnitId, requiredQuantity);

            ApiResponse<Boolean> response = ApiResponse.success(
                    "Kiểm tra tồn kho thành công", isAvailable);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra tồn kho: ", e);

            ApiResponse<Boolean> response = ApiResponse.error("Không thể kiểm tra tồn kho: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy danh sách tồn kho có số lượng thấp
     */
    @GetMapping("/low-stock")
    @Operation(summary = "Lấy danh sách tồn kho thấp", description = "Lấy danh sách các sản phẩm có tồn kho thấp hơn ngưỡng cảnh báo")
    public ResponseEntity<ApiResponse<Page<WarehouseDto>>> getLowStockWarehouses(
            @Parameter(description = "Ngưỡng số lượng tối thiểu") @RequestParam(defaultValue = "10") Integer minQuantity,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "20") int size) {

        log.info("API lấy danh sách tồn kho thấp với ngưỡng: {}", minQuantity);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("quantityOnHand").ascending());
            Page<WarehouseDto> warehouses = warehouseService.getLowStockWarehouses(minQuantity, pageable);

            ApiResponse<Page<WarehouseDto>> response = ApiResponse.success(warehouses);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách tồn kho thấp: ", e);

            ApiResponse<Page<WarehouseDto>> response = ApiResponse
                    .error("Không thể lấy danh sách tồn kho thấp: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy danh sách tồn kho hết hàng
     */
    @GetMapping("/out-of-stock")
    @Operation(summary = "Lấy danh sách hết hàng", description = "Lấy danh sách các sản phẩm đã hết hàng (số lượng = 0)")
    public ResponseEntity<ApiResponse<Page<WarehouseDto>>> getOutOfStockWarehouses(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "20") int size) {

        log.info("API lấy danh sách hết hàng");

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
            Page<WarehouseDto> warehouses = warehouseService.getOutOfStockWarehouses(pageable);

            ApiResponse<Page<WarehouseDto>> response = ApiResponse.success(warehouses);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách hết hàng: ", e);

            ApiResponse<Page<WarehouseDto>> response = ApiResponse
                    .error("Không thể lấy danh sách hết hàng: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy danh sách tất cả tồn kho
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách tất cả tồn kho", description = "Lấy danh sách tất cả tồn kho với phân trang")
    public ResponseEntity<ApiResponse<Page<WarehouseDto>>> getAllWarehouses(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "updatedAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {

        log.info("API lấy danh sách tất cả tồn kho: page={}, size={}", page, size);

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<WarehouseDto> warehouses = warehouseService.getAllWarehouses(pageable);

            ApiResponse<Page<WarehouseDto>> response = ApiResponse.success(warehouses);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách tồn kho: ", e);

            ApiResponse<Page<WarehouseDto>> response = ApiResponse
                    .error("Không thể lấy danh sách tồn kho: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Tìm kiếm tồn kho theo từ khóa
     */
    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm tồn kho", description = "Tìm kiếm tồn kho theo tên sản phẩm, mã biến thể hoặc mã vạch")
    public ResponseEntity<ApiResponse<Page<WarehouseDto>>> searchWarehouses(
            @Parameter(description = "Từ khóa tìm kiếm", required = true) @RequestParam String keyword,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "20") int size) {

        log.info("API tìm kiếm tồn kho với từ khóa: {}", keyword);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
            Page<WarehouseDto> warehouses = warehouseService.searchWarehouses(keyword, pageable);

            ApiResponse<Page<WarehouseDto>> response = ApiResponse.success(warehouses);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm tồn kho: ", e);

            ApiResponse<Page<WarehouseDto>> response = ApiResponse
                    .error("Không thể tìm kiếm tồn kho: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy danh sách tồn kho theo sản phẩm
     */
    @GetMapping("/product/{productId}")
    @Operation(summary = "Lấy tồn kho theo sản phẩm", description = "Lấy danh sách tồn kho của tất cả biến thể của một sản phẩm")
    public ResponseEntity<ApiResponse<List<WarehouseDto>>> getWarehousesByProductId(
            @Parameter(description = "ID của sản phẩm", required = true) @PathVariable Long productId) {

        log.info("API lấy tồn kho theo sản phẩm ID: {}", productId);

        try {
            List<WarehouseDto> warehouses = warehouseService.getWarehousesByProductId(productId);

            ApiResponse<List<WarehouseDto>> response = ApiResponse.success(
                    "Lấy danh sách tồn kho thành công", warehouses);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy tồn kho theo sản phẩm: ", e);

            ApiResponse<List<WarehouseDto>> response = ApiResponse
                    .error("Không thể lấy danh sách tồn kho: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // ==================== WAREHOUSE TRANSACTION APIs ====================

    /**
     * Lấy lịch sử giao dịch kho theo đơn vị sản phẩm
     */
    @GetMapping("/transactions/product-unit/{productUnitId}")
    @Operation(summary = "Lấy lịch sử giao dịch theo đơn vị sản phẩm", description = "Lấy lịch sử giao dịch kho của một đơn vị sản phẩm")
    public ResponseEntity<ApiResponse<Page<WarehouseTransactionDto>>> getTransactionsByProductUnitId(
            @Parameter(description = "ID của đơn vị sản phẩm", required = true) @PathVariable Long productUnitId,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "20") int size) {

        log.info("API lấy lịch sử giao dịch cho đơn vị sản phẩm ID: {}", productUnitId);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
            Page<WarehouseTransactionDto> transactions = warehouseService.getTransactionsByProductUnitId(productUnitId,
                    pageable);

            ApiResponse<Page<WarehouseTransactionDto>> response = ApiResponse.success(transactions);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy lịch sử giao dịch: ", e);

            ApiResponse<Page<WarehouseTransactionDto>> response = ApiResponse
                    .error("Không thể lấy lịch sử giao dịch: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy lịch sử giao dịch kho theo loại giao dịch
     */
    @GetMapping("/transactions/type/{transactionType}")
    @Operation(summary = "Lấy giao dịch theo loại", description = "Lấy lịch sử giao dịch kho theo loại giao dịch")
    public ResponseEntity<ApiResponse<Page<WarehouseTransactionDto>>> getTransactionsByType(
            @Parameter(description = "Loại giao dịch", required = true) @PathVariable WarehouseTransaction.TransactionType transactionType,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "20") int size) {

        log.info("API lấy giao dịch theo loại: {}", transactionType);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
            Page<WarehouseTransactionDto> transactions = warehouseService.getTransactionsByType(transactionType,
                    pageable);

            ApiResponse<Page<WarehouseTransactionDto>> response = ApiResponse.success(transactions);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy giao dịch theo loại: ", e);

            ApiResponse<Page<WarehouseTransactionDto>> response = ApiResponse
                    .error("Không thể lấy giao dịch: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy lịch sử giao dịch kho theo mã tham chiếu
     */
    @GetMapping("/transactions/reference/{referenceId}")
    @Operation(summary = "Lấy giao dịch theo mã tham chiếu", description = "Lấy lịch sử giao dịch kho theo mã tham chiếu (mã phiếu nhập, mã đơn hàng, v.v.)")
    public ResponseEntity<ApiResponse<List<WarehouseTransactionDto>>> getTransactionsByReferenceId(
            @Parameter(description = "Mã tham chiếu", required = true) @PathVariable String referenceId) {

        log.info("API lấy giao dịch theo mã tham chiếu: {}", referenceId);

        try {
            List<WarehouseTransactionDto> transactions = warehouseService.getTransactionsByReferenceId(referenceId);

            ApiResponse<List<WarehouseTransactionDto>> response = ApiResponse.success(
                    "Lấy giao dịch thành công", transactions);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy giao dịch theo mã tham chiếu: ", e);

            ApiResponse<List<WarehouseTransactionDto>> response = ApiResponse
                    .error("Không thể lấy giao dịch: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy lịch sử giao dịch kho trong khoảng thời gian
     */
    @GetMapping("/transactions/date-range")
    @Operation(summary = "Lấy giao dịch theo khoảng thời gian", description = "Lấy lịch sử giao dịch kho trong khoảng thời gian cụ thể")
    public ResponseEntity<ApiResponse<Page<WarehouseTransactionDto>>> getTransactionsByDateRange(
            @Parameter(description = "Ngày bắt đầu (yyyy-MM-dd'T'HH:mm:ss)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "Ngày kết thúc (yyyy-MM-dd'T'HH:mm:ss)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "20") int size) {

        log.info("API lấy giao dịch từ {} đến {}", startDate, endDate);

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
            Page<WarehouseTransactionDto> transactions = warehouseService.getTransactionsByDateRange(startDate, endDate,
                    pageable);

            ApiResponse<Page<WarehouseTransactionDto>> response = ApiResponse.success(transactions);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy giao dịch theo khoảng thời gian: ", e);

            ApiResponse<Page<WarehouseTransactionDto>> response = ApiResponse
                    .error("Không thể lấy giao dịch: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Lấy tất cả giao dịch kho
     */
    @GetMapping("/transactions")
    @Operation(summary = "Lấy tất cả giao dịch kho", description = "Lấy danh sách tất cả giao dịch kho với phân trang")
    public ResponseEntity<ApiResponse<Page<WarehouseTransactionDto>>> getAllTransactions(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "20") int size) {

        log.info("API lấy tất cả giao dịch kho");

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());
            Page<WarehouseTransactionDto> transactions = warehouseService.getAllTransactions(pageable);

            ApiResponse<Page<WarehouseTransactionDto>> response = ApiResponse.success(transactions);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy tất cả giao dịch: ", e);

            ApiResponse<Page<WarehouseTransactionDto>> response = ApiResponse
                    .error("Không thể lấy giao dịch: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
