package iuh.fit.supermarket.controller;

import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.dto.promotion.*;
import iuh.fit.supermarket.enums.PromotionStatus;
import iuh.fit.supermarket.enums.PromotionType;
import iuh.fit.supermarket.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller để quản lý các API liên quan đến promotion
 */
@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Promotion Management", description = "APIs quản lý chương trình khuyến mãi")
public class PromotionController {

    private final PromotionService promotionService;

    // ==================== PROMOTION HEADER ENDPOINTS ====================

    @PostMapping
    @Operation(summary = "Tạo chương trình khuyến mãi mới", description = "Tạo một chương trình khuyến mãi mới với các dòng và chi tiết khuyến mãi")
    public ResponseEntity<ApiResponse<PromotionHeaderDTO>> createPromotion(
            @Valid @RequestBody PromotionCreateRequest request) {
        log.info("API: Tạo chương trình khuyến mãi mới - {}", request.getName());

        PromotionHeaderDTO promotion = promotionService.createPromotion(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo chương trình khuyến mãi thành công", promotion));
    }

    @PutMapping("/{promotionId}")
    @Operation(summary = "Cập nhật chương trình khuyến mãi", description = "Cập nhật thông tin chương trình khuyến mãi")
    public ResponseEntity<ApiResponse<PromotionHeaderDTO>> updatePromotion(
            @Parameter(description = "ID chương trình khuyến mãi") @PathVariable Long promotionId,
            @Valid @RequestBody PromotionCreateRequest request) {
        log.info("API: Cập nhật chương trình khuyến mãi ID: {}", promotionId);

        PromotionHeaderDTO promotion = promotionService.updatePromotion(promotionId, request);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật chương trình khuyến mãi thành công", promotion));
    }

    @DeleteMapping("/{promotionId}")
    @Operation(summary = "Xóa chương trình khuyến mãi", description = "Xóa chương trình khuyến mãi theo ID")
    public ResponseEntity<ApiResponse<String>> deletePromotion(
            @Parameter(description = "ID chương trình khuyến mãi") @PathVariable Long promotionId) {
        log.info("API: Xóa chương trình khuyến mãi ID: {}", promotionId);

        promotionService.deletePromotion(promotionId);

        return ResponseEntity.ok(ApiResponse.success("Xóa chương trình khuyến mãi thành công", null));
    }

    @GetMapping("/{promotionId}")
    @Operation(summary = "Lấy thông tin chương trình khuyến mãi", description = "Lấy thông tin chi tiết chương trình khuyến mãi theo ID")
    public ResponseEntity<ApiResponse<PromotionHeaderDTO>> getPromotionById(
            @Parameter(description = "ID chương trình khuyến mãi") @PathVariable Long promotionId) {
        log.info("API: Lấy thông tin chương trình khuyến mãi ID: {}", promotionId);

        PromotionHeaderDTO promotion = promotionService.getPromotionById(promotionId);

        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin chương trình khuyến mãi thành công", promotion));
    }

    @GetMapping
    @Operation(summary = "Lấy danh sách chương trình khuyến mãi", description = "Lấy danh sách tất cả chương trình khuyến mãi với phân trang")
    public ResponseEntity<ApiResponse<Page<PromotionHeaderDTO>>> getAllPromotions(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("API: Lấy danh sách chương trình khuyến mãi - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<PromotionHeaderDTO> promotions = promotionService.getAllPromotions(pageable);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách chương trình khuyến mãi thành công", promotions));
    }

    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm chương trình khuyến mãi", description = "Tìm kiếm chương trình khuyến mãi theo tên")
    public ResponseEntity<ApiResponse<Page<PromotionHeaderDTO>>> searchPromotions(
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam String keyword,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "20") int size) {
        log.info("API: Tìm kiếm chương trình khuyến mãi với từ khóa: {}", keyword);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PromotionHeaderDTO> promotions = promotionService.searchPromotionsByName(keyword, pageable);

        return ResponseEntity.ok(ApiResponse.success("Tìm kiếm chương trình khuyến mãi thành công", promotions));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Lấy chương trình khuyến mãi theo trạng thái", description = "Lấy danh sách chương trình khuyến mãi theo trạng thái")
    public ResponseEntity<ApiResponse<Page<PromotionHeaderDTO>>> getPromotionsByStatus(
            @Parameter(description = "Trạng thái khuyến mãi") @PathVariable PromotionStatus status,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "20") int size) {
        log.info("API: Lấy chương trình khuyến mãi theo trạng thái: {}", status);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PromotionHeaderDTO> promotions = promotionService.getPromotionsByStatus(status, pageable);

        return ResponseEntity
                .ok(ApiResponse.success("Lấy chương trình khuyến mãi theo trạng thái thành công", promotions));
    }

    @GetMapping("/active")
    @Operation(summary = "Lấy chương trình khuyến mãi đang hoạt động", description = "Lấy danh sách chương trình khuyến mãi đang hoạt động")
    public ResponseEntity<ApiResponse<List<PromotionHeaderDTO>>> getCurrentActivePromotions() {
        log.info("API: Lấy chương trình khuyến mãi đang hoạt động");

        List<PromotionHeaderDTO> promotions = promotionService.getCurrentActivePromotions();

        return ResponseEntity
                .ok(ApiResponse.success("Lấy chương trình khuyến mãi đang hoạt động thành công", promotions));
    }

    @GetMapping("/expiring")
    @Operation(summary = "Lấy chương trình khuyến mãi sắp hết hạn", description = "Lấy danh sách chương trình khuyến mãi sắp hết hạn trong số ngày nhất định")
    public ResponseEntity<ApiResponse<List<PromotionHeaderDTO>>> getPromotionsExpiringWithin(
            @Parameter(description = "Số ngày") @RequestParam(defaultValue = "7") int days) {
        log.info("API: Lấy chương trình khuyến mãi sắp hết hạn trong {} ngày", days);

        List<PromotionHeaderDTO> promotions = promotionService.getPromotionsExpiringWithin(days);

        return ResponseEntity.ok(ApiResponse.success("Lấy chương trình khuyến mãi sắp hết hạn thành công", promotions));
    }

    // ==================== PROMOTION LINE ENDPOINTS ====================

    @PostMapping("/{promotionId}/lines")
    @Operation(summary = "Tạo dòng khuyến mãi mới", description = "Tạo dòng khuyến mãi mới cho chương trình khuyến mãi")
    public ResponseEntity<ApiResponse<PromotionLineDTO>> createPromotionLine(
            @Parameter(description = "ID chương trình khuyến mãi") @PathVariable Long promotionId,
            @Valid @RequestBody PromotionLineCreateRequest request) {
        log.info("API: Tạo dòng khuyến mãi mới cho promotion ID: {}", promotionId);

        PromotionLineDTO promotionLine = promotionService.createPromotionLine(promotionId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo dòng khuyến mãi thành công", promotionLine));
    }

    @PutMapping("/lines/{lineId}")
    @Operation(summary = "Cập nhật dòng khuyến mãi", description = "Cập nhật thông tin dòng khuyến mãi")
    public ResponseEntity<ApiResponse<PromotionLineDTO>> updatePromotionLine(
            @Parameter(description = "ID dòng khuyến mãi") @PathVariable Long lineId,
            @Valid @RequestBody PromotionLineCreateRequest request) {
        log.info("API: Cập nhật dòng khuyến mãi ID: {}", lineId);

        PromotionLineDTO promotionLine = promotionService.updatePromotionLine(lineId, request);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật dòng khuyến mãi thành công", promotionLine));
    }

    @DeleteMapping("/lines/{lineId}")
    @Operation(summary = "Xóa dòng khuyến mãi", description = "Xóa dòng khuyến mãi theo ID")
    public ResponseEntity<ApiResponse<String>> deletePromotionLine(
            @Parameter(description = "ID dòng khuyến mãi") @PathVariable Long lineId) {
        log.info("API: Xóa dòng khuyến mãi ID: {}", lineId);

        promotionService.deletePromotionLine(lineId);

        return ResponseEntity.ok(ApiResponse.success("Xóa dòng khuyến mãi thành công", null));
    }

    @GetMapping("/lines/{lineId}")
    @Operation(summary = "Lấy thông tin dòng khuyến mãi", description = "Lấy thông tin chi tiết dòng khuyến mãi theo ID")
    public ResponseEntity<ApiResponse<PromotionLineDTO>> getPromotionLineById(
            @Parameter(description = "ID dòng khuyến mãi") @PathVariable Long lineId) {
        log.info("API: Lấy thông tin dòng khuyến mãi ID: {}", lineId);

        PromotionLineDTO promotionLine = promotionService.getPromotionLineById(lineId);

        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin dòng khuyến mãi thành công", promotionLine));
    }

    @GetMapping("/{promotionId}/lines")
    @Operation(summary = "Lấy danh sách dòng khuyến mãi", description = "Lấy danh sách dòng khuyến mãi theo promotion ID")
    public ResponseEntity<ApiResponse<List<PromotionLineDTO>>> getPromotionLinesByPromotionId(
            @Parameter(description = "ID chương trình khuyến mãi") @PathVariable Long promotionId) {
        log.info("API: Lấy danh sách dòng khuyến mãi cho promotion ID: {}", promotionId);

        List<PromotionLineDTO> promotionLines = promotionService.getPromotionLinesByPromotionId(promotionId);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách dòng khuyến mãi thành công", promotionLines));
    }

    @GetMapping("/lines/type/{promotionType}")
    @Operation(summary = "Lấy dòng khuyến mãi theo loại", description = "Lấy danh sách dòng khuyến mãi theo loại khuyến mãi")
    public ResponseEntity<ApiResponse<List<PromotionLineDTO>>> getPromotionLinesByType(
            @Parameter(description = "Loại khuyến mãi") @PathVariable PromotionType promotionType) {
        log.info("API: Lấy dòng khuyến mãi theo loại: {}", promotionType);

        List<PromotionLineDTO> promotionLines = promotionService.getPromotionLinesByType(promotionType);

        return ResponseEntity.ok(ApiResponse.success("Lấy dòng khuyến mãi theo loại thành công", promotionLines));
    }

    // ==================== PROMOTION DETAIL ENDPOINTS ====================

    @PostMapping("/lines/{lineId}/details")
    @Operation(summary = "Tạo chi tiết khuyến mãi mới", description = "Tạo chi tiết khuyến mãi mới cho dòng khuyến mãi")
    public ResponseEntity<ApiResponse<PromotionDetailDTO>> createPromotionDetail(
            @Parameter(description = "ID dòng khuyến mãi") @PathVariable Long lineId,
            @Valid @RequestBody PromotionDetailCreateRequest request) {
        log.info("API: Tạo chi tiết khuyến mãi mới cho line ID: {}", lineId);

        PromotionDetailDTO promotionDetail = promotionService.createPromotionDetail(lineId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo chi tiết khuyến mãi thành công", promotionDetail));
    }

    @PutMapping("/details/{detailId}")
    @Operation(summary = "Cập nhật chi tiết khuyến mãi", description = "Cập nhật thông tin chi tiết khuyến mãi")
    public ResponseEntity<ApiResponse<PromotionDetailDTO>> updatePromotionDetail(
            @Parameter(description = "ID chi tiết khuyến mãi") @PathVariable Long detailId,
            @Valid @RequestBody PromotionDetailCreateRequest request) {
        log.info("API: Cập nhật chi tiết khuyến mãi ID: {}", detailId);

        PromotionDetailDTO promotionDetail = promotionService.updatePromotionDetail(detailId, request);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật chi tiết khuyến mãi thành công", promotionDetail));
    }

    @DeleteMapping("/details/{detailId}")
    @Operation(summary = "Xóa chi tiết khuyến mãi", description = "Xóa chi tiết khuyến mãi theo ID")
    public ResponseEntity<ApiResponse<String>> deletePromotionDetail(
            @Parameter(description = "ID chi tiết khuyến mãi") @PathVariable Long detailId) {
        log.info("API: Xóa chi tiết khuyến mãi ID: {}", detailId);

        promotionService.deletePromotionDetail(detailId);

        return ResponseEntity.ok(ApiResponse.success("Xóa chi tiết khuyến mãi thành công", null));
    }

    @GetMapping("/lines/{lineId}/details")
    @Operation(summary = "Lấy danh sách chi tiết khuyến mãi", description = "Lấy danh sách chi tiết khuyến mãi theo line ID")
    public ResponseEntity<ApiResponse<List<PromotionDetailDTO>>> getPromotionDetailsByLineId(
            @Parameter(description = "ID dòng khuyến mãi") @PathVariable Long lineId) {
        log.info("API: Lấy danh sách chi tiết khuyến mãi cho line ID: {}", lineId);

        List<PromotionDetailDTO> promotionDetails = promotionService.getPromotionDetailsByLineId(lineId);

        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách chi tiết khuyến mãi thành công", promotionDetails));
    }

    // ==================== BUSINESS LOGIC ENDPOINTS ====================

    @PostMapping("/calculate-discount")
    @Operation(summary = "Tính toán discount cho đơn hàng", description = "Tính toán discount và gift items cho đơn hàng")
    public ResponseEntity<ApiResponse<PromotionDiscountResult>> calculateDiscount(
            @Valid @RequestBody CalculateDiscountRequest request) {
        log.info("API: Tính toán discount cho đơn hàng với giá trị: {}", request.getOrderAmount());

        PromotionDiscountResult result = promotionService.calculateDiscount(
                request.getOrderAmount(), request.getOrderItems());

        return ResponseEntity.ok(ApiResponse.success("Tính toán discount thành công", result));
    }

    @PostMapping("/calculate-product-discount")
    @Operation(summary = "Tính toán discount cho sản phẩm", description = "Tính toán discount cho sản phẩm cụ thể")
    public ResponseEntity<ApiResponse<PromotionDiscountResult>> calculateProductDiscount(
            @Valid @RequestBody CalculateProductDiscountRequest request) {
        log.info("API: Tính toán discount cho sản phẩm ID: {}", request.getProductUnitId());

        PromotionDiscountResult result = promotionService.calculateProductDiscount(
                request.getProductUnitId(), request.getQuantity(), request.getUnitPrice());

        return ResponseEntity.ok(ApiResponse.success("Tính toán discount sản phẩm thành công", result));
    }

    @GetMapping("/applicable/product/{productUnitId}")
    @Operation(summary = "Lấy khuyến mãi áp dụng cho sản phẩm", description = "Lấy danh sách khuyến mãi có thể áp dụng cho sản phẩm")
    public ResponseEntity<ApiResponse<List<PromotionLineDTO>>> getApplicablePromotionsForProduct(
            @Parameter(description = "ID product unit") @PathVariable Long productUnitId) {
        log.info("API: Lấy khuyến mãi áp dụng cho sản phẩm ID: {}", productUnitId);

        List<PromotionLineDTO> promotions = promotionService.getApplicablePromotionsForProduct(productUnitId);

        return ResponseEntity.ok(ApiResponse.success("Lấy khuyến mãi áp dụng cho sản phẩm thành công", promotions));
    }

    @GetMapping("/applicable/category/{categoryId}")
    @Operation(summary = "Lấy khuyến mãi áp dụng cho danh mục", description = "Lấy danh sách khuyến mãi có thể áp dụng cho danh mục")
    public ResponseEntity<ApiResponse<List<PromotionLineDTO>>> getApplicablePromotionsForCategory(
            @Parameter(description = "ID category") @PathVariable Long categoryId) {
        log.info("API: Lấy khuyến mãi áp dụng cho danh mục ID: {}", categoryId);

        List<PromotionLineDTO> promotions = promotionService.getApplicablePromotionsForCategory(categoryId);

        return ResponseEntity.ok(ApiResponse.success("Lấy khuyến mãi áp dụng cho danh mục thành công", promotions));
    }

    // ==================== UTILITY ENDPOINTS ====================

    @PutMapping("/{promotionId}/activate")
    @Operation(summary = "Kích hoạt chương trình khuyến mãi", description = "Kích hoạt chương trình khuyến mãi")
    public ResponseEntity<ApiResponse<PromotionHeaderDTO>> activatePromotion(
            @Parameter(description = "ID chương trình khuyến mãi") @PathVariable Long promotionId) {
        log.info("API: Kích hoạt chương trình khuyến mãi ID: {}", promotionId);

        PromotionHeaderDTO promotion = promotionService.activatePromotion(promotionId);

        return ResponseEntity.ok(ApiResponse.success("Kích hoạt chương trình khuyến mãi thành công", promotion));
    }

    @PutMapping("/{promotionId}/pause")
    @Operation(summary = "Tạm dừng chương trình khuyến mãi", description = "Tạm dừng chương trình khuyến mãi")
    public ResponseEntity<ApiResponse<PromotionHeaderDTO>> pausePromotion(
            @Parameter(description = "ID chương trình khuyến mãi") @PathVariable Long promotionId) {
        log.info("API: Tạm dừng chương trình khuyến mãi ID: {}", promotionId);

        PromotionHeaderDTO promotion = promotionService.pausePromotion(promotionId);

        return ResponseEntity.ok(ApiResponse.success("Tạm dừng chương trình khuyến mãi thành công", promotion));
    }

    @PutMapping("/{promotionId}/expire")
    @Operation(summary = "Kết thúc chương trình khuyến mãi", description = "Kết thúc chương trình khuyến mãi")
    public ResponseEntity<ApiResponse<PromotionHeaderDTO>> expirePromotion(
            @Parameter(description = "ID chương trình khuyến mãi") @PathVariable Long promotionId) {
        log.info("API: Kết thúc chương trình khuyến mãi ID: {}", promotionId);

        PromotionHeaderDTO promotion = promotionService.expirePromotion(promotionId);

        return ResponseEntity.ok(ApiResponse.success("Kết thúc chương trình khuyến mãi thành công", promotion));
    }

    @PostMapping("/bulk")
    @Operation(summary = "Tạo nhiều chương trình khuyến mãi", description = "Tạo nhiều chương trình khuyến mãi cùng lúc")
    public ResponseEntity<ApiResponse<List<PromotionHeaderDTO>>> createBulkPromotions(
            @Valid @RequestBody List<PromotionCreateRequest> requests) {
        log.info("API: Tạo bulk {} chương trình khuyến mãi", requests.size());

        List<PromotionHeaderDTO> promotions = promotionService.createBulkPromotions(requests);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo bulk chương trình khuyến mãi thành công", promotions));
    }

    @PutMapping("/bulk/status")
    @Operation(summary = "Cập nhật trạng thái nhiều chương trình khuyến mãi", description = "Cập nhật trạng thái cho nhiều chương trình khuyến mãi")
    public ResponseEntity<ApiResponse<String>> updateBulkPromotionStatus(
            @Valid @RequestBody BulkUpdateStatusRequest request) {
        log.info("API: Cập nhật trạng thái bulk {} chương trình khuyến mãi", request.getPromotionIds().size());

        promotionService.updateBulkPromotionStatus(request.getPromotionIds(), request.getStatus());

        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái bulk thành công", null));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Lấy thống kê chương trình khuyến mãi", description = "Lấy thống kê tổng quan về chương trình khuyến mãi")
    public ResponseEntity<ApiResponse<PromotionStatisticsDTO>> getPromotionStatistics() {
        log.info("API: Lấy thống kê chương trình khuyến mãi");

        PromotionStatisticsDTO statistics = promotionService.getPromotionStatistics();

        return ResponseEntity.ok(ApiResponse.success("Lấy thống kê chương trình khuyến mãi thành công", statistics));
    }
}
