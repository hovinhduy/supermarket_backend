package iuh.fit.supermarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.dto.promotion.*;
import iuh.fit.supermarket.enums.PromotionStatus;
import iuh.fit.supermarket.enums.PromotionType;
import iuh.fit.supermarket.service.PromotionService;
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
 * Controller xử lý các API liên quan đến quản lý khuyến mãi
 */
@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Promotion Management", description = "API quản lý chương trình khuyến mãi")
@SecurityRequirement(name = "Bearer Authentication")
public class PromotionController {

    private final PromotionService promotionService;

    /**
     * Tạo mới chương trình khuyến mãi (chỉ ADMIN và MANAGER)
     */
    @Operation(summary = "Tạo mới chương trình khuyến mãi", description = "Tạo mới chương trình khuyến mãi. Chỉ ADMIN và MANAGER mới có quyền.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tạo thành công", content = @Content(schema = @Schema(implementation = PromotionHeaderDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<PromotionHeaderDTO>> createPromotion(
            @Valid @RequestBody PromotionCreateRequest request) {
        log.info("Nhận yêu cầu tạo chương trình khuyến mãi: {}", request.getPromotionCode());

        PromotionHeaderDTO createdPromotion = promotionService.createPromotion(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo chương trình khuyến mãi thành công", createdPromotion));
    }

    /**
     * Cập nhật chương trình khuyến mãi (chỉ ADMIN và MANAGER)
     */
    @Operation(summary = "Cập nhật chương trình khuyến mãi", description = "Cập nhật thông tin chương trình khuyến mãi. Chỉ ADMIN và MANAGER mới có quyền.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công", content = @Content(schema = @Schema(implementation = PromotionHeaderDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy chương trình khuyến mãi"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<PromotionHeaderDTO>> updatePromotion(
            @Parameter(description = "ID chương trình khuyến mãi") @PathVariable Long id,
            @Valid @RequestBody PromotionUpdateRequest request) {
        log.info("Nhận yêu cầu cập nhật chương trình khuyến mãi ID: {}", id);

        PromotionHeaderDTO updatedPromotion = promotionService.updatePromotion(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật chương trình khuyến mãi thành công", updatedPromotion));
    }

    /**
     * Xóa chương trình khuyến mãi (chỉ ADMIN)
     */
    @Operation(summary = "Xóa chương trình khuyến mãi", description = "Xóa chương trình khuyến mãi. Chỉ ADMIN mới có quyền.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy chương trình khuyến mãi"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePromotion(
            @Parameter(description = "ID chương trình khuyến mãi") @PathVariable Long id) {
        log.info("Nhận yêu cầu xóa chương trình khuyến mãi ID: {}", id);

        promotionService.deletePromotion(id);
        return ResponseEntity.ok(
                ApiResponse.success("Xóa chương trình khuyến mãi thành công", null));
    }

    /**
     * Lấy thông tin chi tiết chương trình khuyến mãi theo ID
     */
    @Operation(summary = "Lấy chi tiết chương trình khuyến mãi", description = "Lấy thông tin chi tiết chương trình khuyến mãi theo ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy thành công", content = @Content(schema = @Schema(implementation = PromotionHeaderDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy chương trình khuyến mãi")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<ApiResponse<PromotionHeaderDTO>> getPromotionById(
            @Parameter(description = "ID chương trình khuyến mãi") @PathVariable Long id) {
        log.info("Nhận yêu cầu lấy thông tin chương trình khuyến mãi ID: {}", id);

        PromotionHeaderDTO promotion = promotionService.getPromotionById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Lấy thông tin chương trình khuyến mãi thành công", promotion));
    }

    /**
     * Lấy thông tin chi tiết chương trình khuyến mãi theo mã
     */
    @Operation(summary = "Lấy chi tiết chương trình khuyến mãi theo mã", description = "Lấy thông tin chi tiết chương trình khuyến mãi theo mã code")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy thành công", content = @Content(schema = @Schema(implementation = PromotionHeaderDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy chương trình khuyến mãi")
    })
    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<ApiResponse<PromotionHeaderDTO>> getPromotionByCode(
            @Parameter(description = "Mã chương trình khuyến mãi") @PathVariable String code) {
        log.info("Nhận yêu cầu lấy thông tin chương trình khuyến mãi theo mã: {}", code);

        PromotionHeaderDTO promotion = promotionService.getPromotionByCode(code);
        return ResponseEntity.ok(
                ApiResponse.success("Lấy thông tin chương trình khuyến mãi thành công", promotion));
    }

    /**
     * Lấy tất cả chương trình khuyến mãi với phân trang
     */
    @Operation(summary = "Lấy danh sách tất cả chương trình khuyến mãi", description = "Lấy danh sách tất cả chương trình khuyến mãi với phân trang và sắp xếp")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công", content = @Content(schema = @Schema(implementation = PromotionHeaderDTO.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<ApiResponse<Page<PromotionHeaderDTO>>> getAllPromotions(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp (ASC hoặc DESC)") @RequestParam(defaultValue = "DESC") String sortDirection) {
        log.info("Nhận yêu cầu lấy danh sách tất cả chương trình khuyến mãi - Page: {}, Size: {}",
                page, size);

        Page<PromotionHeaderDTO> promotions = promotionService.getAllPromotions(
                page, size, sortBy, sortDirection);
        return ResponseEntity.ok(
                ApiResponse.success("Lấy danh sách chương trình khuyến mãi thành công", promotions));
    }

    /**
     * Tìm kiếm chương trình khuyến mãi theo từ khóa
     */
    @Operation(summary = "Tìm kiếm chương trình khuyến mãi", description = "Tìm kiếm chương trình khuyến mãi theo tên hoặc mã")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tìm kiếm thành công", content = @Content(schema = @Schema(implementation = PromotionHeaderDTO.class)))
    })
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<ApiResponse<Page<PromotionHeaderDTO>>> searchPromotions(
            @Parameter(description = "Từ khóa tìm kiếm") @RequestParam String keyword,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "10") Integer size) {
        log.info("Nhận yêu cầu tìm kiếm chương trình khuyến mãi với từ khóa: {}", keyword);

        Page<PromotionHeaderDTO> promotions = promotionService.searchPromotions(keyword, page, size);
        return ResponseEntity.ok(
                ApiResponse.success("Tìm kiếm chương trình khuyến mãi thành công", promotions));
    }

    /**
     * Tìm kiếm nâng cao chương trình khuyến mãi
     */
    @Operation(summary = "Tìm kiếm nâng cao chương trình khuyến mãi", description = "Tìm kiếm chương trình khuyến mãi theo nhiều tiêu chí")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tìm kiếm thành công", content = @Content(schema = @Schema(implementation = PromotionHeaderDTO.class)))
    })
    @PostMapping("/search/advanced")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<ApiResponse<Page<PromotionHeaderDTO>>> searchPromotionsAdvanced(
            @Valid @RequestBody PromotionSearchRequest searchRequest) {
        log.info("Nhận yêu cầu tìm kiếm nâng cao chương trình khuyến mãi");

        Page<PromotionHeaderDTO> promotions = promotionService.searchPromotionsAdvanced(searchRequest);
        return ResponseEntity.ok(
                ApiResponse.success("Tìm kiếm chương trình khuyến mãi thành công", promotions));
    }

    /**
     * Lấy danh sách khuyến mãi theo trạng thái
     */
    @Operation(summary = "Lấy danh sách khuyến mãi theo trạng thái", description = "Lấy danh sách khuyến mãi lọc theo trạng thái")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công", content = @Content(schema = @Schema(implementation = PromotionHeaderDTO.class)))
    })
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<ApiResponse<Page<PromotionHeaderDTO>>> getPromotionsByStatus(
            @Parameter(description = "Trạng thái khuyến mãi") @PathVariable PromotionStatus status,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "10") Integer size) {
        log.info("Nhận yêu cầu lấy danh sách khuyến mãi theo trạng thái: {}", status);

        Page<PromotionHeaderDTO> promotions = promotionService.getPromotionsByStatus(status, page, size);
        return ResponseEntity.ok(
                ApiResponse.success("Lấy danh sách khuyến mãi thành công", promotions));
    }

    /**
     * Lấy danh sách khuyến mãi theo loại
     */
    @Operation(summary = "Lấy danh sách khuyến mãi theo loại", description = "Lấy danh sách khuyến mãi lọc theo loại")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công", content = @Content(schema = @Schema(implementation = PromotionHeaderDTO.class)))
    })
    @GetMapping("/type/{type}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<ApiResponse<Page<PromotionHeaderDTO>>> getPromotionsByType(
            @Parameter(description = "Loại khuyến mãi") @PathVariable PromotionType type,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "Số lượng bản ghi mỗi trang") @RequestParam(defaultValue = "10") Integer size) {
        log.info("Nhận yêu cầu lấy danh sách khuyến mãi theo loại: {}", type);

        Page<PromotionHeaderDTO> promotions = promotionService.getPromotionsByType(type, page, size);
        return ResponseEntity.ok(
                ApiResponse.success("Lấy danh sách khuyến mãi thành công", promotions));
    }

    /**
     * Lấy danh sách khuyến mãi đang hoạt động
     */
    @Operation(summary = "Lấy danh sách khuyến mãi đang hoạt động", description = "Lấy danh sách khuyến mãi đang trong thời gian hoạt động và có trạng thái ACTIVE")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công", content = @Content(schema = @Schema(implementation = PromotionHeaderDTO.class)))
    })
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'CASHIER')")
    public ResponseEntity<ApiResponse<List<PromotionHeaderDTO>>> getActivePromotions() {
        log.info("Nhận yêu cầu lấy danh sách khuyến mãi đang hoạt động");

        List<PromotionHeaderDTO> promotions = promotionService.getActivePromotions();
        return ResponseEntity.ok(
                ApiResponse.success("Lấy danh sách khuyến mãi đang hoạt động thành công", promotions));
    }

    /**
     * Lấy danh sách khuyến mãi sắp hết hạn
     */
    @Operation(summary = "Lấy danh sách khuyến mãi sắp hết hạn", description = "Lấy danh sách khuyến mãi sắp hết hạn trong N ngày tới")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Lấy danh sách thành công", content = @Content(schema = @Schema(implementation = PromotionHeaderDTO.class)))
    })
    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<PromotionHeaderDTO>>> getExpiringPromotions(
            @Parameter(description = "Số ngày") @RequestParam(defaultValue = "7") int days) {
        log.info("Nhận yêu cầu lấy danh sách khuyến mãi sắp hết hạn trong {} ngày", days);

        List<PromotionHeaderDTO> promotions = promotionService.getExpiringPromotions(days);
        return ResponseEntity.ok(
                ApiResponse.success("Lấy danh sách khuyến mãi sắp hết hạn thành công", promotions));
    }

    /**
     * Cập nhật trạng thái chương trình khuyến mãi
     */
    @Operation(summary = "Cập nhật trạng thái khuyến mãi", description = "Cập nhật trạng thái chương trình khuyến mãi. Chỉ ADMIN và MANAGER mới có quyền.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công", content = @Content(schema = @Schema(implementation = PromotionHeaderDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy chương trình khuyến mãi"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<PromotionHeaderDTO>> updatePromotionStatus(
            @Parameter(description = "ID chương trình khuyến mãi") @PathVariable Long id,
            @Parameter(description = "Trạng thái mới") @RequestParam PromotionStatus status) {
        log.info("Nhận yêu cầu cập nhật trạng thái chương trình khuyến mãi ID {} sang {}", id, status);

        PromotionHeaderDTO updatedPromotion = promotionService.updatePromotionStatus(id, status);
        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật trạng thái chương trình khuyến mãi thành công", updatedPromotion));
    }

    /**
     * Kiểm tra mã khuyến mãi đã tồn tại
     */
    @Operation(summary = "Kiểm tra mã khuyến mãi đã tồn tại", description = "Kiểm tra xem mã khuyến mãi đã tồn tại trong hệ thống chưa")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Kiểm tra thành công")
    })
    @GetMapping("/check-code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Boolean>> checkPromotionCodeExists(
            @Parameter(description = "Mã khuyến mãi cần kiểm tra") @PathVariable String code) {
        log.info("Nhận yêu cầu kiểm tra mã khuyến mãi: {}", code);

        boolean exists = promotionService.isPromotionCodeExists(code);
        return ResponseEntity.ok(
                ApiResponse.success("Kiểm tra mã khuyến mãi thành công", exists));
    }

    /**
     * Đếm số lượng khuyến mãi theo trạng thái
     */
    @Operation(summary = "Đếm số lượng khuyến mãi theo trạng thái", description = "Đếm số lượng chương trình khuyến mãi theo trạng thái")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Đếm thành công")
    })
    @GetMapping("/count/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Long>> countByStatus(
            @Parameter(description = "Trạng thái khuyến mãi") @PathVariable PromotionStatus status) {
        log.info("Nhận yêu cầu đếm số lượng khuyến mãi theo trạng thái: {}", status);

        long count = promotionService.countByStatus(status);
        return ResponseEntity.ok(
                ApiResponse.success("Đếm số lượng khuyến mãi thành công", count));
    }
}

