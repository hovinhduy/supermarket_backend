package iuh.fit.supermarket.controller;

import iuh.fit.supermarket.annotation.RequireRole;
import iuh.fit.supermarket.dto.promotion.*;
import iuh.fit.supermarket.enums.EmployeeRole;
import iuh.fit.supermarket.service.PromotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller cho quản lý chương trình khuyến mãi
 * Cung cấp các API endpoints để tạo, cập nhật, xóa và truy vấn khuyến mãi
 */
@RestController
@RequestMapping("/api/promotions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Promotion Management", description = "APIs quản lý chương trình khuyến mãi")
public class PromotionController {

    private final PromotionService promotionService;

    /**
     * Tạo mới chỉ promotion header (không bao gồm lines)
     * 
     * @param requestDTO thông tin header cần tạo
     * @return ResponseEntity chứa thông tin header đã tạo
     */
    @PostMapping("/headers")
    @RequireRole({ EmployeeRole.ADMIN, EmployeeRole.MANAGER })
    @Operation(summary = "Tạo promotion header mới", description = "Tạo chỉ promotion header (không bao gồm lines). Sau đó có thể thêm lines riêng biệt.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo promotion header thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @ApiResponse(responseCode = "409", description = "Tên chương trình đã tồn tại"),
            @ApiResponse(responseCode = "403", description = "Không có quyền thực hiện thao tác này")
    })
    public ResponseEntity<iuh.fit.supermarket.dto.common.ApiResponse<PromotionHeaderResponseDTO>> createPromotionHeader(
            @Valid @RequestBody PromotionHeaderOnlyRequestDTO requestDTO) {

        log.info("API: Tạo promotion header mới - {}", requestDTO.getPromotionName());

        PromotionHeaderResponseDTO responseDTO = promotionService.createPromotionHeaderOnly(requestDTO);

        log.info("API: Đã tạo thành công promotion header ID: {}", responseDTO.getPromotionId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(iuh.fit.supermarket.dto.common.ApiResponse.success("Tạo promotion header thành công",
                        responseDTO));
    }

    /**
     * Tạo mới promotion line (có thể bao gồm detail hoặc không)
     * 
     * @param headerId   ID của promotion header (từ URL)
     * @param requestDTO thông tin line cần tạo (có thể bao gồm detail)
     * @return ResponseEntity chứa thông tin line đã tạo
     */
    @PostMapping("/headers/{headerId}/lines")
    @RequireRole({ EmployeeRole.ADMIN, EmployeeRole.MANAGER })
    @Operation(summary = "Tạo promotion line mới", description = "Tạo promotion line cho một header đã tồn tại. " +
            "Có thể bao gồm detail trong request (trường 'detail' là optional). " +
            "Nếu không truyền detail, có thể thêm detail sau bằng API riêng.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo promotion line thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy promotion header"),
            @ApiResponse(responseCode = "409", description = "Mã khuyến mãi đã tồn tại"),
            @ApiResponse(responseCode = "403", description = "Không có quyền thực hiện thao tác này")
    })
    public ResponseEntity<iuh.fit.supermarket.dto.common.ApiResponse<PromotionLineResponseDTO>> createPromotionLine(
            @Parameter(description = "ID của promotion header", required = true) @PathVariable Long headerId,
            @Valid @RequestBody PromotionLineOnlyRequestDTO requestDTO) {

        log.info("API: Tạo promotion line mới cho header ID: {} - {}", headerId, requestDTO.getPromotionCode());

        PromotionLineResponseDTO responseDTO = promotionService.createPromotionLineOnly(headerId, requestDTO);

        log.info("API: Đã tạo thành công promotion line ID: {}", responseDTO.getPromotionLineId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(iuh.fit.supermarket.dto.common.ApiResponse.success("Tạo promotion line thành công", responseDTO));
    }

    /**
     * Tạo mới promotion detail cho một line đã tồn tại
     * 
     * @param lineId     ID của promotion line (từ URL)
     * @param requestDTO thông tin detail cần tạo
     * @return ResponseEntity chứa thông tin detail đã tạo
     */
    @PostMapping("/lines/{lineId}/details")
    @RequireRole({ EmployeeRole.ADMIN, EmployeeRole.MANAGER })
    @Operation(summary = "Tạo promotion detail mới", description = "Tạo promotion detail cho một line đã tồn tại")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo promotion detail thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ hoặc line đã có detail"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy promotion line"),
            @ApiResponse(responseCode = "403", description = "Không có quyền thực hiện thao tác này")
    })
    public ResponseEntity<iuh.fit.supermarket.dto.common.ApiResponse<PromotionDetailResponseDTO>> createPromotionDetail(
            @Parameter(description = "ID của promotion line", required = true) @PathVariable Long lineId,
            @Valid @RequestBody PromotionDetailWithLineRequestDTO requestDTO) {

        log.info("API: Tạo promotion detail mới cho line ID: {}", lineId);

        PromotionDetailResponseDTO responseDTO = promotionService.createPromotionDetail(lineId, requestDTO);

        log.info("API: Đã tạo thành công promotion detail ID: {}", responseDTO.getDetailId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(iuh.fit.supermarket.dto.common.ApiResponse.success("Tạo promotion detail thành công",
                        responseDTO));
    }

    /**
     * Tạo mới chương trình khuyến mãi (API cũ - bao gồm header, lines và details)
     * 
     * @param requestDTO thông tin chương trình khuyến mãi cần tạo
     * @return ResponseEntity chứa thông tin chương trình đã tạo
     */
    @PostMapping
    @RequireRole({ EmployeeRole.ADMIN, EmployeeRole.MANAGER })
    @Operation(summary = "Tạo chương trình khuyến mãi mới (Full)", description = "Tạo một chương trình khuyến mãi mới với đầy đủ thông tin header, lines và details. Đây là API tổng hợp, bạn cũng có thể tạo từng phần riêng biệt bằng các API /headers, /lines, /details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo chương trình khuyến mãi thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @ApiResponse(responseCode = "409", description = "Tên chương trình hoặc mã khuyến mãi đã tồn tại"),
            @ApiResponse(responseCode = "403", description = "Không có quyền thực hiện thao tác này")
    })
    public ResponseEntity<iuh.fit.supermarket.dto.common.ApiResponse<PromotionHeaderResponseDTO>> createPromotion(
            @Valid @RequestBody PromotionHeaderRequestDTO requestDTO) {

        log.info("API: Tạo chương trình khuyến mãi mới (full) - {}", requestDTO.getPromotionName());

        PromotionHeaderResponseDTO responseDTO = promotionService.createPromotion(requestDTO);

        log.info("API: Đã tạo thành công chương trình khuyến mãi ID: {}", responseDTO.getPromotionId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(iuh.fit.supermarket.dto.common.ApiResponse.success("Tạo chương trình khuyến mãi thành công",
                        responseDTO));
    }

    /**
     * Cập nhật thông tin promotion header (chỉ cập nhật header, không cập nhật
     * lines)
     * 
     * @param promotionId ID của promotion header cần cập nhật
     * @param requestDTO  thông tin cập nhật (chỉ thông tin header)
     * @return ResponseEntity chứa thông tin header đã cập nhật
     */
    @PutMapping("/headers/{promotionId}")
    @RequireRole({ EmployeeRole.ADMIN, EmployeeRole.MANAGER })
    @Operation(summary = "Cập nhật promotion header", description = "Cập nhật chỉ thông tin của promotion header, không ảnh hưởng đến các lines")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật promotion header thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy promotion header"),
            @ApiResponse(responseCode = "409", description = "Tên chương trình đã tồn tại"),
            @ApiResponse(responseCode = "403", description = "Không có quyền thực hiện thao tác này")
    })
    public ResponseEntity<iuh.fit.supermarket.dto.common.ApiResponse<PromotionHeaderResponseDTO>> updatePromotionHeader(
            @Parameter(description = "ID của promotion header", required = true) @PathVariable Long promotionId,
            @Valid @RequestBody PromotionHeaderOnlyRequestDTO requestDTO) {

        log.info("API: Cập nhật promotion header ID: {} - {}", promotionId, requestDTO.getPromotionName());

        PromotionHeaderResponseDTO responseDTO = promotionService.updatePromotionHeaderOnly(promotionId, requestDTO);

        log.info("API: Đã cập nhật thành công promotion header ID: {}", promotionId);
        return ResponseEntity.ok(iuh.fit.supermarket.dto.common.ApiResponse
                .success("Cập nhật promotion header thành công", responseDTO));
    }

    /**
     * Cập nhật promotion line (bao gồm cả detail nếu có)
     * 
     * @param lineId     ID của promotion line cần cập nhật
     * @param requestDTO thông tin cập nhật (bao gồm cả detail)
     * @return ResponseEntity chứa thông tin line đã cập nhật
     */
    @PutMapping("/lines/{lineId}")
    @RequireRole({ EmployeeRole.ADMIN, EmployeeRole.MANAGER })
    @Operation(summary = "Cập nhật promotion line", description = "Cập nhật thông tin của promotion line và detail của nó")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật promotion line thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy promotion line"),
            @ApiResponse(responseCode = "409", description = "Mã khuyến mãi đã tồn tại"),
            @ApiResponse(responseCode = "403", description = "Không có quyền thực hiện thao tác này")
    })
    public ResponseEntity<iuh.fit.supermarket.dto.common.ApiResponse<PromotionLineResponseDTO>> updatePromotionLine(
            @Parameter(description = "ID của promotion line", required = true) @PathVariable Long lineId,
            @Valid @RequestBody PromotionLineOnlyRequestDTO requestDTO) {

        log.info("API: Cập nhật promotion line ID: {} - {}", lineId, requestDTO.getPromotionCode());

        PromotionLineResponseDTO responseDTO = promotionService.updatePromotionLine(lineId, requestDTO);

        log.info("API: Đã cập nhật thành công promotion line ID: {}", lineId);
        return ResponseEntity.ok(
                iuh.fit.supermarket.dto.common.ApiResponse.success("Cập nhật promotion line thành công", responseDTO));
    }

    /**
     * Cập nhật promotion detail
     * 
     * @param detailId   ID của promotion detail cần cập nhật
     * @param requestDTO thông tin detail cập nhật
     * @return ResponseEntity chứa thông tin detail đã cập nhật
     */
    @PutMapping("/details/{detailId}")
    @RequireRole({ EmployeeRole.ADMIN, EmployeeRole.MANAGER })
    @Operation(summary = "Cập nhật promotion detail", description = "Cập nhật thông tin chi tiết khuyến mãi của một line")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật promotion detail thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy promotion detail"),
            @ApiResponse(responseCode = "403", description = "Không có quyền thực hiện thao tác này")
    })
    public ResponseEntity<iuh.fit.supermarket.dto.common.ApiResponse<PromotionDetailResponseDTO>> updatePromotionDetail(
            @Parameter(description = "ID của promotion detail", required = true) @PathVariable Long detailId,
            @Valid @RequestBody PromotionDetailRequestDTO requestDTO) {

        log.info("API: Cập nhật promotion detail ID: {}", detailId);

        PromotionDetailResponseDTO responseDTO = promotionService.updatePromotionDetail(detailId, requestDTO);

        log.info("API: Đã cập nhật thành công promotion detail ID: {}", detailId);
        return ResponseEntity.ok(iuh.fit.supermarket.dto.common.ApiResponse
                .success("Cập nhật promotion detail thành công", responseDTO));
    }

    /**
     * Cập nhật toàn bộ chương trình khuyến mãi (API cũ - bao gồm header, lines và
     * details)
     * 
     * @param promotionId ID của chương trình cần cập nhật
     * @param requestDTO  thông tin cập nhật đầy đủ
     * @return ResponseEntity chứa thông tin đã cập nhật
     * @deprecated Nên sử dụng các API cập nhật riêng biệt cho header, line và
     *             detail
     */
    @PutMapping("/{promotionId}")
    @RequireRole({ EmployeeRole.ADMIN, EmployeeRole.MANAGER })
    @Operation(summary = "Cập nhật toàn bộ chương trình khuyến mãi (Full)", description = "Cập nhật toàn bộ thông tin của chương trình khuyến mãi bao gồm header, lines và details. Đây là API tổng hợp, nên sử dụng các API cập nhật riêng biệt để cập nhật từng phần.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật chương trình khuyến mãi thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu đầu vào không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chương trình khuyến mãi"),
            @ApiResponse(responseCode = "409", description = "Tên chương trình hoặc mã khuyến mãi đã tồn tại"),
            @ApiResponse(responseCode = "403", description = "Không có quyền thực hiện thao tác này")
    })
    @Deprecated
    public ResponseEntity<iuh.fit.supermarket.dto.common.ApiResponse<PromotionHeaderResponseDTO>> updatePromotion(
            @Parameter(description = "ID của chương trình khuyến mãi", required = true) @PathVariable Long promotionId,
            @Valid @RequestBody PromotionHeaderRequestDTO requestDTO) {

        log.info("API: Cập nhật toàn bộ chương trình khuyến mãi ID: {} - {}", promotionId,
                requestDTO.getPromotionName());

        PromotionHeaderResponseDTO responseDTO = promotionService.updatePromotion(promotionId, requestDTO);

        log.info("API: Đã cập nhật thành công chương trình khuyến mãi ID: {}", promotionId);
        return ResponseEntity.ok(iuh.fit.supermarket.dto.common.ApiResponse
                .success("Cập nhật chương trình khuyến mãi thành công", responseDTO));
    }

    /**
     * Xóa chương trình khuyến mãi
     * 
     * @param promotionId ID của chương trình cần xóa
     * @return ResponseEntity với ApiResponse
     */
    @DeleteMapping("/{promotionId}")
    @RequireRole({ EmployeeRole.ADMIN, EmployeeRole.MANAGER })
    @Operation(summary = "Xóa chương trình khuyến mãi", description = "Xóa một chương trình khuyến mãi (chỉ cho phép xóa khi chưa bắt đầu)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa chương trình khuyến mãi thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chương trình khuyến mãi"),
            @ApiResponse(responseCode = "400", description = "Không thể xóa chương trình khuyến mãi này"),
            @ApiResponse(responseCode = "403", description = "Không có quyền thực hiện thao tác này")
    })
    public ResponseEntity<iuh.fit.supermarket.dto.common.ApiResponse<Void>> deletePromotion(
            @Parameter(description = "ID của chương trình khuyến mãi", required = true) @PathVariable Long promotionId) {

        log.info("API: Xóa chương trình khuyến mãi ID: {}", promotionId);

        promotionService.deletePromotion(promotionId);

        log.info("API: Đã xóa thành công chương trình khuyến mãi ID: {}", promotionId);
        return ResponseEntity
                .ok(iuh.fit.supermarket.dto.common.ApiResponse.success("Xóa chương trình khuyến mãi thành công", null));
    }

    /**
     * Lấy thông tin chi tiết chương trình khuyến mãi
     * 
     * @param promotionId ID của chương trình
     * @return ResponseEntity chứa thông tin chi tiết
     */
    @GetMapping("/{promotionId}")
    @RequireRole({ EmployeeRole.ADMIN, EmployeeRole.MANAGER, EmployeeRole.STAFF })
    @Operation(summary = "Lấy thông tin chi tiết chương trình khuyến mãi", description = "Lấy thông tin đầy đủ của một chương trình khuyến mãi bao gồm tất cả lines và details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy chương trình khuyến mãi"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<iuh.fit.supermarket.dto.common.ApiResponse<PromotionHeaderResponseDTO>> getPromotionById(
            @Parameter(description = "ID của chương trình khuyến mãi", required = true) @PathVariable Long promotionId) {

        log.debug("API: Lấy thông tin chương trình khuyến mãi ID: {}", promotionId);

        PromotionHeaderResponseDTO responseDTO = promotionService.getPromotionById(promotionId);

        return ResponseEntity.ok(iuh.fit.supermarket.dto.common.ApiResponse.success(responseDTO));
    }

    /**
     * Lấy thông tin chi tiết promotion line và detail của nó
     * 
     * @param lineId ID của promotion line
     * @return ResponseEntity chứa thông tin chi tiết line và detail
     */
    @GetMapping("/lines/{lineId}")
    @RequireRole({ EmployeeRole.ADMIN, EmployeeRole.MANAGER, EmployeeRole.STAFF })
    @Operation(summary = "Lấy thông tin chi tiết promotion line", description = "Lấy thông tin đầy đủ của một promotion line bao gồm detail của nó")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy promotion line"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<iuh.fit.supermarket.dto.common.ApiResponse<PromotionLineResponseDTO>> getPromotionLineById(
            @Parameter(description = "ID của promotion line", required = true) @PathVariable Long lineId) {

        log.debug("API: Lấy thông tin promotion line ID: {}", lineId);

        PromotionLineResponseDTO responseDTO = promotionService.getPromotionLineById(lineId);

        return ResponseEntity.ok(iuh.fit.supermarket.dto.common.ApiResponse.success(responseDTO));
    }

    /**
     * Xóa promotion line và detail của nó
     * 
     * @param lineId ID của promotion line cần xóa
     * @return ResponseEntity với ApiResponse
     */
    @DeleteMapping("/lines/{lineId}")
    @RequireRole({ EmployeeRole.ADMIN, EmployeeRole.MANAGER })
    @Operation(summary = "Xóa promotion line", description = "Xóa một promotion line và detail của nó (chỉ cho phép xóa khi chưa được sử dụng và chưa hoạt động)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa promotion line thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy promotion line"),
            @ApiResponse(responseCode = "400", description = "Không thể xóa promotion line này (đang hoạt động hoặc đã được sử dụng)"),
            @ApiResponse(responseCode = "403", description = "Không có quyền thực hiện thao tác này")
    })
    public ResponseEntity<iuh.fit.supermarket.dto.common.ApiResponse<Void>> deletePromotionLine(
            @Parameter(description = "ID của promotion line", required = true) @PathVariable Long lineId) {

        log.info("API: Xóa promotion line ID: {}", lineId);

        promotionService.deletePromotionLine(lineId);

        log.info("API: Đã xóa thành công promotion line ID: {}", lineId);
        return ResponseEntity
                .ok(iuh.fit.supermarket.dto.common.ApiResponse.success("Xóa promotion line thành công", null));
    }

    /**
     * Lấy danh sách promotion lines theo promotion header ID với lọc
     * 
     * @param promotionId   ID của promotion header
     * @param promotionType Loại khuyến mãi để lọc (optional)
     * @param startDateFrom Ngày bắt đầu từ (optional)
     * @param startDateTo   Ngày bắt đầu đến (optional)
     * @param endDateFrom   Ngày kết thúc từ (optional)
     * @param endDateTo     Ngày kết thúc đến (optional)
     * @return ResponseEntity chứa danh sách promotion lines với details
     */
    @GetMapping("/{promotionId}/lines")
    @RequireRole({ EmployeeRole.ADMIN, EmployeeRole.MANAGER, EmployeeRole.STAFF })
    @Operation(summary = "Lấy danh sách lines theo header ID", description = "Lấy danh sách các promotion lines và details của một promotion header với khả năng lọc theo loại và ngày")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy promotion header"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<iuh.fit.supermarket.dto.common.ApiResponse<List<PromotionLineResponseDTO>>> getPromotionLinesByHeaderId(
            @Parameter(description = "ID của promotion header", required = true) @PathVariable Long promotionId,

            @Parameter(description = "Loại khuyến mãi để lọc") @RequestParam(required = false) String promotionType,

            @Parameter(description = "Ngày bắt đầu từ (yyyy-MM-dd HH:mm:ss)") @RequestParam(required = false) String startDateFrom,

            @Parameter(description = "Ngày bắt đầu đến (yyyy-MM-dd HH:mm:ss)") @RequestParam(required = false) String startDateTo,

            @Parameter(description = "Ngày kết thúc từ (yyyy-MM-dd HH:mm:ss)") @RequestParam(required = false) String endDateFrom,

            @Parameter(description = "Ngày kết thúc đến (yyyy-MM-dd HH:mm:ss)") @RequestParam(required = false) String endDateTo) {

        log.debug("API: Lấy danh sách promotion lines cho header ID: {}", promotionId);

        // Parse promotion type nếu có
        iuh.fit.supermarket.enums.PromotionType type = null;
        if (promotionType != null && !promotionType.trim().isEmpty()) {
            try {
                type = iuh.fit.supermarket.enums.PromotionType.valueOf(promotionType.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Loại khuyến mãi không hợp lệ: {}", promotionType);
            }
        }

        // Parse dates
        java.time.LocalDateTime startFrom = parseDateTime(startDateFrom);
        java.time.LocalDateTime startTo = parseDateTime(startDateTo);
        java.time.LocalDateTime endFrom = parseDateTime(endDateFrom);
        java.time.LocalDateTime endTo = parseDateTime(endDateTo);

        List<PromotionLineResponseDTO> lines = promotionService.getPromotionLinesByHeaderId(
                promotionId, type, startFrom, startTo, endFrom, endTo);

        log.debug("API: Tìm thấy {} promotion lines", lines.size());
        return ResponseEntity.ok(iuh.fit.supermarket.dto.common.ApiResponse.success(lines));
    }

    /**
     * Tìm kiếm và lọc chương trình khuyến mãi với phân trang
     * 
     * @param searchDTO điều kiện tìm kiếm và phân trang
     * @return ResponseEntity chứa danh sách chương trình khuyến mãi
     */
    @GetMapping
    @RequireRole({ EmployeeRole.ADMIN, EmployeeRole.MANAGER, EmployeeRole.STAFF })
    @Operation(summary = "Tìm kiếm chương trình khuyến mãi", description = "Tìm kiếm và lọc chương trình khuyến mãi với nhiều điều kiện và hỗ trợ phân trang")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm kiếm thành công"),
            @ApiResponse(responseCode = "400", description = "Tham số tìm kiếm không hợp lệ"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    public ResponseEntity<iuh.fit.supermarket.dto.common.ApiResponse<Page<PromotionHeaderResponseDTO>>> searchPromotions(
            @Parameter(description = "Từ khóa tìm kiếm (tên hoặc mô tả)") @RequestParam(required = false) String keyword,

            @Parameter(description = "Trạng thái chương trình") @RequestParam(required = false) String status,

            @Parameter(description = "Loại khuyến mãi") @RequestParam(required = false) String promotionType,

            @Parameter(description = "Ngày bắt đầu từ (yyyy-MM-dd HH:mm:ss)") @RequestParam(required = false) String startDateFrom,

            @Parameter(description = "Ngày bắt đầu đến (yyyy-MM-dd HH:mm:ss)") @RequestParam(required = false) String startDateTo,

            @Parameter(description = "Ngày kết thúc từ (yyyy-MM-dd HH:mm:ss)") @RequestParam(required = false) String endDateFrom,

            @Parameter(description = "Ngày kết thúc đến (yyyy-MM-dd HH:mm:ss)") @RequestParam(required = false) String endDateTo,

            @Parameter(description = "Chỉ lấy chương trình đang hoạt động") @RequestParam(required = false) Boolean activeOnly,

            @Parameter(description = "Chỉ lấy chương trình sắp diễn ra") @RequestParam(required = false) Boolean upcomingOnly,

            @Parameter(description = "Chỉ lấy chương trình đã hết hạn") @RequestParam(required = false) Boolean expiredOnly,

            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") Integer page,

            @Parameter(description = "Kích thước trang") @RequestParam(defaultValue = "20") Integer size,

            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Hướng sắp xếp (asc/desc)") @RequestParam(defaultValue = "desc") String sortDirection) {

        log.debug("API: Tìm kiếm chương trình khuyến mãi với từ khóa: {}", keyword);

        // Tạo DTO tìm kiếm từ các tham số
        PromotionSearchDTO searchDTO = createSearchDTO(keyword, status, promotionType,
                startDateFrom, startDateTo, endDateFrom, endDateTo,
                activeOnly, upcomingOnly, expiredOnly,
                page, size, sortBy, sortDirection);

        Page<PromotionHeaderResponseDTO> result = promotionService.searchPromotions(searchDTO);

        log.debug("API: Tìm thấy {} chương trình khuyến mãi", result.getTotalElements());
        return ResponseEntity.ok(iuh.fit.supermarket.dto.common.ApiResponse.success(result));
    }

    /**
     * Tạo PromotionSearchDTO từ các tham số request
     */
    private PromotionSearchDTO createSearchDTO(String keyword, String status, String promotionType,
            String startDateFrom, String startDateTo, String endDateFrom, String endDateTo,
            Boolean activeOnly, Boolean upcomingOnly, Boolean expiredOnly,
            Integer page, Integer size, String sortBy, String sortDirection) {

        PromotionSearchDTO searchDTO = new PromotionSearchDTO();

        // Thiết lập các điều kiện tìm kiếm
        searchDTO.setKeyword(keyword);

        // Parse enum values
        if (status != null && !status.trim().isEmpty()) {
            try {
                searchDTO.setStatus(iuh.fit.supermarket.enums.PromotionStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Trạng thái không hợp lệ: {}", status);
            }
        }

        if (promotionType != null && !promotionType.trim().isEmpty()) {
            try {
                searchDTO
                        .setPromotionType(iuh.fit.supermarket.enums.PromotionType.valueOf(promotionType.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("Loại khuyến mãi không hợp lệ: {}", promotionType);
            }
        }

        // Parse date values
        searchDTO.setStartDateFrom(parseDateTime(startDateFrom));
        searchDTO.setStartDateTo(parseDateTime(startDateTo));
        searchDTO.setEndDateFrom(parseDateTime(endDateFrom));
        searchDTO.setEndDateTo(parseDateTime(endDateTo));

        // Thiết lập các flag đặc biệt
        searchDTO.setActiveOnly(activeOnly);
        searchDTO.setUpcomingOnly(upcomingOnly);
        searchDTO.setExpiredOnly(expiredOnly);

        // Thiết lập phân trang và sắp xếp
        searchDTO.setPage(page);
        searchDTO.setSize(size);
        searchDTO.setSortBy(sortBy);
        searchDTO.setSortDirection(sortDirection);

        return searchDTO;
    }

    /**
     * Parse chuỗi thời gian thành LocalDateTime
     */
    private java.time.LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }

        try {
            return java.time.LocalDateTime.parse(dateTimeStr,
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            log.warn("Không thể parse thời gian: {}", dateTimeStr);
            return null;
        }
    }
}
