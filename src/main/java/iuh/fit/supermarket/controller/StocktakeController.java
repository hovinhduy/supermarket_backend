//package iuh.fit.supermarket.controller;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import iuh.fit.supermarket.dto.common.ApiResponse;
//import iuh.fit.supermarket.dto.stocktake.*;
//import iuh.fit.supermarket.enums.StocktakeStatus;
//import iuh.fit.supermarket.service.StocktakeService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
///**
// * Controller cho quản lý kiểm kê kho
// */
//@RestController
//@RequestMapping("/api/stocktakes")
//@RequiredArgsConstructor
//@Slf4j
//@Tag(name = "Stocktake Management", description = "API quản lý kiểm kê kho")
//@SecurityRequirement(name = "Bearer Authentication")
//public class StocktakeController {
//
//    private final StocktakeService stocktakeService;
//
//    /**
//     * Tạo phiếu kiểm kê mới
//     */
//    @PostMapping
//    @Operation(summary = "Tạo phiếu kiểm kê mới", description = "Tạo phiếu kiểm kê mới với trạng thái PENDING. " +
//            "Có thể cung cấp mã phiếu kiểm kê tùy chỉnh hoặc để hệ thống tự động sinh mã.")
//    public ResponseEntity<ApiResponse<StocktakeDto>> createStocktake(
//            @Valid @RequestBody StocktakeCreateRequest request) {
//
//        log.info("API tạo phiếu kiểm kê mới được gọi với {} chi tiết",
//                request.getStocktakeDetails() != null ? request.getStocktakeDetails().size() : 0);
//
//        try {
//            StocktakeDto stocktake = stocktakeService.createStocktake(request);
//            return ResponseEntity.status(HttpStatus.CREATED)
//                    .body(ApiResponse.success("Tạo phiếu kiểm kê thành công", stocktake));
//
//        } catch (Exception e) {
//            log.error("Lỗi khi tạo phiếu kiểm kê: ", e);
//            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
//        }
//    }
//
//    /**
//     * Lấy thông tin phiếu kiểm kê theo ID
//     */
//    @GetMapping("/{stocktakeId}")
//    @Operation(summary = "Lấy thông tin phiếu kiểm kê", description = "Lấy thông tin chi tiết phiếu kiểm kê theo ID")
//    public ResponseEntity<ApiResponse<StocktakeDto>> getStocktakeById(
//            @Parameter(description = "ID phiếu kiểm kê", required = true) @PathVariable Integer stocktakeId) {
//
//        log.info("API lấy thông tin phiếu kiểm kê ID: {}", stocktakeId);
//
//        try {
//            StocktakeDto stocktake = stocktakeService.getStocktakeById(stocktakeId);
//            return ResponseEntity.ok(ApiResponse.success("Lấy thông tin phiếu kiểm kê thành công", stocktake));
//
//        } catch (Exception e) {
//            log.error("Lỗi khi lấy thông tin phiếu kiểm kê: ", e);
//            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
//        }
//    }
//
//    /**
//     * Lấy thông tin phiếu kiểm kê theo mã phiếu
//     */
//    @GetMapping("/code/{stocktakeCode}")
//    @Operation(summary = "Lấy thông tin phiếu kiểm kê theo mã", description = "Lấy thông tin chi tiết phiếu kiểm kê theo mã phiếu")
//    public ResponseEntity<ApiResponse<StocktakeDto>> getStocktakeByCode(
//            @Parameter(description = "Mã phiếu kiểm kê", required = true) @PathVariable String stocktakeCode) {
//
//        log.info("API lấy thông tin phiếu kiểm kê mã: {}", stocktakeCode);
//
//        try {
//            StocktakeDto stocktake = stocktakeService.getStocktakeByCode(stocktakeCode);
//            return ResponseEntity.ok(ApiResponse.success("Lấy thông tin phiếu kiểm kê thành công", stocktake));
//
//        } catch (Exception e) {
//            log.error("Lỗi khi lấy thông tin phiếu kiểm kê: ", e);
//            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
//        }
//    }
//
//    /**
//     * Cập nhật thông tin phiếu kiểm kê
//     */
//    @PutMapping("/{stocktakeId}")
//    @Operation(summary = "Cập nhật phiếu kiểm kê", description = "Cập nhật thông tin phiếu kiểm kê (chỉ cho phép khi trạng thái PENDING)")
//    public ResponseEntity<ApiResponse<StocktakeDto>> updateStocktake(
//            @Parameter(description = "ID phiếu kiểm kê", required = true) @PathVariable Integer stocktakeId,
//            @Valid @RequestBody StocktakeUpdateRequest request) {
//
//        log.info("API cập nhật phiếu kiểm kê ID: {}", stocktakeId);
//
//        try {
//            StocktakeDto stocktake = stocktakeService.updateStocktake(stocktakeId, request);
//            return ResponseEntity.ok(ApiResponse.success("Cập nhật phiếu kiểm kê thành công", stocktake));
//
//        } catch (Exception e) {
//            log.error("Lỗi khi cập nhật phiếu kiểm kê: ", e);
//            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
//        }
//    }
//
//    /**
//     * Hoàn thành phiếu kiểm kê
//     */
//    @PostMapping("/{stocktakeId}/complete")
//    @Operation(summary = "Hoàn thành phiếu kiểm kê", description = "Hoàn thành phiếu kiểm kê và cập nhật tồn kho")
//    public ResponseEntity<ApiResponse<StocktakeDto>> completeStocktake(
//            @Parameter(description = "ID phiếu kiểm kê", required = true) @PathVariable Integer stocktakeId) {
//
//        log.info("API hoàn thành phiếu kiểm kê ID: {}", stocktakeId);
//
//        try {
//            StocktakeDto stocktake = stocktakeService.completeStocktake(stocktakeId);
//            return ResponseEntity.ok(ApiResponse.success("Hoàn thành phiếu kiểm kê thành công", stocktake));
//
//        } catch (Exception e) {
//            log.error("Lỗi khi hoàn thành phiếu kiểm kê: ", e);
//            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
//        }
//    }
//
//    /**
//     * Cập nhật số lượng tồn kho mới nhất cho phiếu kiểm kê
//     */
//    @PostMapping("/{stocktakeId}/refresh-expected-quantities")
//    @Operation(summary = "Cập nhật số lượng tồn kho mới nhất", description = "Cập nhật số lượng tồn kho mới nhất cho phiếu kiểm kê đang PENDING. "
//            +
//            "Đảm bảo quantityExpected phản ánh đúng số lượng tồn kho hiện tại từ cơ sở dữ liệu.")
//    public ResponseEntity<ApiResponse<StocktakeDto>> refreshExpectedQuantities(
//            @Parameter(description = "ID phiếu kiểm kê", required = true) @PathVariable Integer stocktakeId) {
//
//        log.info("API cập nhật số lượng tồn kho mới nhất cho phiếu kiểm kê ID: {}", stocktakeId);
//
//        try {
//            StocktakeDto stocktake = stocktakeService.refreshExpectedQuantities(stocktakeId);
//            return ResponseEntity.ok(ApiResponse.success("Cập nhật số lượng tồn kho mới nhất thành công", stocktake));
//
//        } catch (Exception e) {
//            log.error("Lỗi khi cập nhật số lượng tồn kho mới nhất: ", e);
//            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
//        }
//    }
//
//    /**
//     * Xóa phiếu kiểm kê
//     */
//    @DeleteMapping("/{stocktakeId}")
//    @Operation(summary = "Xóa phiếu kiểm kê", description = "Xóa phiếu kiểm kê (chỉ cho phép khi trạng thái PENDING)")
//    public ResponseEntity<ApiResponse<String>> deleteStocktake(
//            @Parameter(description = "ID phiếu kiểm kê", required = true) @PathVariable Integer stocktakeId) {
//
//        log.info("API xóa phiếu kiểm kê ID: {}", stocktakeId);
//
//        try {
//            stocktakeService.deleteStocktake(stocktakeId);
//            return ResponseEntity.ok(ApiResponse.success("Xóa phiếu kiểm kê thành công", null));
//
//        } catch (Exception e) {
//            log.error("Lỗi khi xóa phiếu kiểm kê: ", e);
//            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
//        }
//    }
//
//    /**
//     * Lấy danh sách phiếu kiểm kê với phân trang
//     */
//    @GetMapping
//    @Operation(summary = "Lấy danh sách phiếu kiểm kê", description = "Lấy danh sách phiếu kiểm kê với phân trang và sắp xếp")
//    public ResponseEntity<ApiResponse<Page<StocktakeDto>>> getAllStocktakes(
//            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
//            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "20") int size,
//            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "createdAt") String sortBy,
//            @Parameter(description = "Hướng sắp xếp (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
//
//        log.info("API lấy danh sách phiếu kiểm kê - page: {}, size: {}", page, size);
//
//        try {
//            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
//            Pageable pageable = PageRequest.of(page, size, sort);
//
//            Page<StocktakeDto> stocktakes = stocktakeService.getAllStocktakes(pageable);
//            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phiếu kiểm kê thành công", stocktakes));
//
//        } catch (Exception e) {
//            log.error("Lỗi khi lấy danh sách phiếu kiểm kê: ", e);
//            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
//        }
//    }
//
//    /**
//     * Lấy danh sách phiếu kiểm kê theo trạng thái
//     */
//    @GetMapping("/status/{status}")
//    @Operation(summary = "Lấy phiếu kiểm kê theo trạng thái", description = "Lấy danh sách phiếu kiểm kê theo trạng thái")
//    public ResponseEntity<ApiResponse<Page<StocktakeDto>>> getStocktakesByStatus(
//            @Parameter(description = "Trạng thái kiểm kê", required = true) @PathVariable StocktakeStatus status,
//            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
//            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "20") int size,
//            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "createdAt") String sortBy,
//            @Parameter(description = "Hướng sắp xếp (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
//
//        log.info("API lấy danh sách phiếu kiểm kê theo trạng thái: {}", status);
//
//        try {
//            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
//            Pageable pageable = PageRequest.of(page, size, sort);
//
//            Page<StocktakeDto> stocktakes = stocktakeService.getStocktakesByStatus(status, pageable);
//            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phiếu kiểm kê thành công", stocktakes));
//
//        } catch (Exception e) {
//            log.error("Lỗi khi lấy danh sách phiếu kiểm kê theo trạng thái: ", e);
//            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
//        }
//    }
//
//    /**
//     * Tìm kiếm phiếu kiểm kê theo từ khóa
//     */
//    @GetMapping("/search")
//    @Operation(summary = "Tìm kiếm phiếu kiểm kê", description = "Tìm kiếm phiếu kiểm kê theo mã phiếu hoặc ghi chú")
//    public ResponseEntity<ApiResponse<Page<StocktakeDto>>> searchStocktakes(
//            @Parameter(description = "Từ khóa tìm kiếm", required = true) @RequestParam String keyword,
//            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
//            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "20") int size,
//            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "createdAt") String sortBy,
//            @Parameter(description = "Hướng sắp xếp (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
//
//        log.info("API tìm kiếm phiếu kiểm kê với từ khóa: {}", keyword);
//
//        try {
//            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
//            Pageable pageable = PageRequest.of(page, size, sort);
//
//            Page<StocktakeDto> stocktakes = stocktakeService.searchStocktakes(keyword, pageable);
//            return ResponseEntity.ok(ApiResponse.success("Tìm kiếm phiếu kiểm kê thành công", stocktakes));
//
//        } catch (Exception e) {
//            log.error("Lỗi khi tìm kiếm phiếu kiểm kê: ", e);
//            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
//        }
//    }
//
//    /**
//     * Lấy danh sách phiếu kiểm kê trong khoảng thời gian
//     */
//    @GetMapping("/date-range")
//    @Operation(summary = "Lấy phiếu kiểm kê theo khoảng thời gian", description = "Lấy danh sách phiếu kiểm kê trong khoảng thời gian")
//    public ResponseEntity<ApiResponse<Page<StocktakeDto>>> getStocktakesByDateRange(
//            @Parameter(description = "Ngày bắt đầu (yyyy-MM-dd'T'HH:mm:ss)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
//            @Parameter(description = "Ngày kết thúc (yyyy-MM-dd'T'HH:mm:ss)", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
//            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
//            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "20") int size,
//            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "createdAt") String sortBy,
//            @Parameter(description = "Hướng sắp xếp (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {
//
//        log.info("API lấy phiếu kiểm kê từ {} đến {}", startDate, endDate);
//
//        try {
//            Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
//            Pageable pageable = PageRequest.of(page, size, sort);
//
//            Page<StocktakeDto> stocktakes = stocktakeService.getStocktakesByDateRange(startDate, endDate, pageable);
//            return ResponseEntity.ok(ApiResponse.success("Lấy danh sách phiếu kiểm kê thành công", stocktakes));
//
//        } catch (Exception e) {
//            log.error("Lỗi khi lấy danh sách phiếu kiểm kê theo khoảng thời gian: ", e);
//            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
//        }
//    }
//
//    /**
//     * Lấy danh sách chi tiết kiểm kê
//     */
//    @GetMapping("/{stocktakeId}/details")
//    @Operation(summary = "Lấy chi tiết kiểm kê", description = "Lấy danh sách chi tiết kiểm kê của một phiếu")
//    public ResponseEntity<ApiResponse<List<StocktakeDetailDto>>> getStocktakeDetails(
//            @Parameter(description = "ID phiếu kiểm kê", required = true) @PathVariable Integer stocktakeId) {
//
//        log.info("API lấy chi tiết kiểm kê cho phiếu ID: {}", stocktakeId);
//
//        try {
//            List<StocktakeDetailDto> details = stocktakeService.getStocktakeDetails(stocktakeId);
//            return ResponseEntity.ok(ApiResponse.success("Lấy chi tiết kiểm kê thành công", details));
//
//        } catch (Exception e) {
//            log.error("Lỗi khi lấy chi tiết kiểm kê: ", e);
//            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
//        }
//    }
//
//    /**
//     * Cập nhật chi tiết kiểm kê
//     */
//    @PutMapping("/details/{detailId}")
//    @Operation(summary = "Cập nhật chi tiết kiểm kê", description = "Cập nhật thông tin chi tiết kiểm kê")
//    public ResponseEntity<ApiResponse<StocktakeDetailDto>> updateStocktakeDetail(
//            @Parameter(description = "ID chi tiết kiểm kê", required = true) @PathVariable Integer detailId,
//            @Parameter(description = "Số lượng thực tế đếm được") @RequestParam(required = false) Integer quantityCounted,
//            @Parameter(description = "Ghi chú lý do chênh lệch") @RequestParam(required = false) String reason) {
//
//        log.info("API cập nhật chi tiết kiểm kê ID: {}", detailId);
//
//        try {
//            StocktakeDetailDto detail = stocktakeService.updateStocktakeDetail(detailId, quantityCounted, reason);
//            return ResponseEntity.ok(ApiResponse.success("Cập nhật chi tiết kiểm kê thành công", detail));
//
//        } catch (Exception e) {
//            log.error("Lỗi khi cập nhật chi tiết kiểm kê: ", e);
//            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
//        }
//    }
//
//    /**
//     * Xóa chi tiết kiểm kê
//     */
//    @DeleteMapping("/details/{detailId}")
//    @Operation(summary = "Xóa chi tiết kiểm kê", description = "Xóa chi tiết kiểm kê khỏi phiếu")
//    public ResponseEntity<ApiResponse<String>> deleteStocktakeDetail(
//            @Parameter(description = "ID chi tiết kiểm kê", required = true) @PathVariable Integer detailId) {
//
//        log.info("API xóa chi tiết kiểm kê ID: {}", detailId);
//
//        try {
//            stocktakeService.deleteStocktakeDetail(detailId);
//            return ResponseEntity.ok(ApiResponse.success("Xóa chi tiết kiểm kê thành công", null));
//
//        } catch (Exception e) {
//            log.error("Lỗi khi xóa chi tiết kiểm kê: ", e);
//            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
//        }
//    }
//
//    /**
//     * Lấy thống kê tổng quan phiếu kiểm kê
//     */
//    @GetMapping("/{stocktakeId}/summary")
//    @Operation(summary = "Lấy thống kê tổng quan", description = "Lấy thống kê tổng quan về phiếu kiểm kê")
//    public ResponseEntity<ApiResponse<StocktakeDto.StocktakeSummary>> getStocktakeSummary(
//            @Parameter(description = "ID phiếu kiểm kê", required = true) @PathVariable Integer stocktakeId) {
//
//        log.info("API lấy thống kê tổng quan cho phiếu ID: {}", stocktakeId);
//
//        try {
//            StocktakeDto.StocktakeSummary summary = stocktakeService.getStocktakeSummary(stocktakeId);
//            return ResponseEntity.ok(ApiResponse.success("Lấy thống kê tổng quan thành công", summary));
//
//        } catch (Exception e) {
//            log.error("Lỗi khi lấy thống kê tổng quan: ", e);
//            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
//        }
//    }
//}
