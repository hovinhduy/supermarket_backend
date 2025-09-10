package iuh.fit.supermarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.dto.product.AttributeCreateRequest;
import iuh.fit.supermarket.dto.product.AttributeDto;
import iuh.fit.supermarket.dto.product.AttributeValueCreateRequest;
import iuh.fit.supermarket.dto.product.AttributeValueDto;
import iuh.fit.supermarket.service.AttributeService;
import iuh.fit.supermarket.service.AttributeValueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý các API liên quan đến quản lý thuộc tính
 */
@RestController
@RequestMapping("/api/attributes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Attribute Management", description = "APIs cho quản lý thuộc tính sản phẩm")
public class AttributeController {

    private final AttributeService attributeService;
    private final AttributeValueService attributeValueService;

    /**
     * Lấy danh sách tất cả thuộc tính
     */
    @GetMapping
    @Operation(summary = "Lấy danh sách thuộc tính", description = "Lấy danh sách tất cả thuộc tính")
    public ResponseEntity<ApiResponse<List<AttributeDto>>> getAllAttributes() {

        log.info("API lấy danh sách thuộc tính được gọi");

        try {
            List<AttributeDto> attributes = attributeService.getAllAttributes();
            return ResponseEntity.ok(ApiResponse.success(attributes));

        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách thuộc tính: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Tạo thuộc tính mới
     */
    @PostMapping
    @Operation(summary = "Tạo thuộc tính mới", description = "Tạo thuộc tính mới")
    public ResponseEntity<ApiResponse<AttributeDto>> createAttribute(
            @RequestBody AttributeCreateRequest request) {

        log.info("API tạo thuộc tính mới được gọi: {}", request.getName());

        try {
            AttributeDto attribute = attributeService.createAttribute(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Tạo thuộc tính thành công", attribute));

        } catch (Exception e) {
            log.error("Lỗi khi tạo thuộc tính: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy thông tin thuộc tính theo ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin thuộc tính", description = "Lấy thông tin chi tiết thuộc tính theo ID")
    public ResponseEntity<ApiResponse<AttributeDto>> getAttributeById(
            @PathVariable Long id) {

        log.info("API lấy thông tin thuộc tính ID: {}", id);

        try {
            AttributeDto attribute = attributeService.getAttributeById(id);
            return ResponseEntity.ok(ApiResponse.success(attribute));

        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin thuộc tính: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Cập nhật thông tin thuộc tính
     */
    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật thuộc tính", description = "Cập nhật thông tin thuộc tính")
    public ResponseEntity<ApiResponse<AttributeDto>> updateAttribute(
            @PathVariable Long id,
            @RequestBody AttributeCreateRequest request) {

        log.info("API cập nhật thuộc tính ID: {}", id);

        try {
            AttributeDto attribute = attributeService.updateAttribute(id, request);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật thuộc tính thành công", attribute));

        } catch (Exception e) {
            log.error("Lỗi khi cập nhật thuộc tính: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Xóa thuộc tính
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa thuộc tính", description = "Xóa thuộc tính")
    public ResponseEntity<ApiResponse<String>> deleteAttribute(
            @PathVariable Long id) {

        log.info("API xóa thuộc tính ID: {}", id);

        try {
            attributeService.deleteAttribute(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa thuộc tính thành công", (String) null));

        } catch (Exception e) {
            log.error("Lỗi khi xóa thuộc tính: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // ======== ATTRIBUTE VALUE ENDPOINTS ========

    /**
     * Lấy danh sách giá trị thuộc tính theo ID thuộc tính
     */
    @GetMapping("/{attributeId}/values")
    @Operation(summary = "Lấy giá trị thuộc tính", description = "Lấy danh sách giá trị thuộc tính theo ID thuộc tính")
    public ResponseEntity<ApiResponse<List<AttributeValueDto>>> getAttributeValuesByAttributeId(
            @PathVariable Long attributeId) {

        log.info("API lấy danh sách giá trị thuộc tính theo ID: {}", attributeId);

        try {
            List<AttributeValueDto> attributeValues = attributeValueService
                    .getAttributeValuesByAttributeId(attributeId);
            return ResponseEntity.ok(ApiResponse.success(attributeValues));

        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách giá trị thuộc tính: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Tạo giá trị thuộc tính mới
     */
    @PostMapping("/values")
    @Operation(summary = "Tạo giá trị thuộc tính mới", description = "Tạo giá trị thuộc tính mới")
    public ResponseEntity<ApiResponse<AttributeValueDto>> createAttributeValue(
            @RequestBody AttributeValueCreateRequest request) {

        log.info("API tạo giá trị thuộc tính mới được gọi: {}", request.getValue());

        try {
            AttributeValueDto attributeValue = attributeValueService.createAttributeValue(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Tạo giá trị thuộc tính thành công", attributeValue));

        } catch (Exception e) {
            log.error("Lỗi khi tạo giá trị thuộc tính: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy thông tin giá trị thuộc tính theo ID
     */
    @GetMapping("/values/{valueId}")
    @Operation(summary = "Lấy thông tin giá trị thuộc tính", description = "Lấy thông tin chi tiết giá trị thuộc tính theo ID")
    public ResponseEntity<ApiResponse<AttributeValueDto>> getAttributeValueById(
            @PathVariable Long valueId) {

        log.info("API lấy thông tin giá trị thuộc tính ID: {}", valueId);

        try {
            AttributeValueDto attributeValue = attributeValueService.getAttributeValueById(valueId);
            return ResponseEntity.ok(ApiResponse.success(attributeValue));

        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin giá trị thuộc tính: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Cập nhật thông tin giá trị thuộc tính
     */
    @PutMapping("/values/{valueId}")
    @Operation(summary = "Cập nhật giá trị thuộc tính", description = "Cập nhật thông tin giá trị thuộc tính")
    public ResponseEntity<ApiResponse<AttributeValueDto>> updateAttributeValue(
            @PathVariable Long valueId,
            @RequestBody AttributeValueCreateRequest request) {

        log.info("API cập nhật giá trị thuộc tính ID: {}", valueId);

        try {
            AttributeValueDto attributeValue = attributeValueService.updateAttributeValue(valueId, request);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật giá trị thuộc tính thành công", attributeValue));

        } catch (Exception e) {
            log.error("Lỗi khi cập nhật giá trị thuộc tính: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Xóa giá trị thuộc tính
     */
    @DeleteMapping("/values/{valueId}")
    @Operation(summary = "Xóa giá trị thuộc tính", description = "Xóa giá trị thuộc tính")
    public ResponseEntity<ApiResponse<String>> deleteAttributeValue(
            @PathVariable Long valueId) {

        log.info("API xóa giá trị thuộc tính ID: {}", valueId);

        try {
            attributeValueService.deleteAttributeValue(valueId);
            return ResponseEntity.ok(ApiResponse.success("Xóa giá trị thuộc tính thành công", (String) null));

        } catch (Exception e) {
            log.error("Lỗi khi xóa giá trị thuộc tính: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Lấy danh sách tất cả giá trị thuộc tính
     */
    @GetMapping("/values")
    @Operation(summary = "Lấy danh sách giá trị thuộc tính", description = "Lấy danh sách tất cả giá trị thuộc tính")
    public ResponseEntity<ApiResponse<List<AttributeValueDto>>> getAllAttributeValues() {

        log.info("API lấy danh sách tất cả giá trị thuộc tính được gọi");

        try {
            List<AttributeValueDto> attributeValues = attributeValueService.getAllAttributeValues();
            return ResponseEntity.ok(ApiResponse.success(attributeValues));

        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách giá trị thuộc tính: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Tìm kiếm giá trị thuộc tính
     */
    @GetMapping("/values/search")
    @Operation(summary = "Tìm kiếm giá trị thuộc tính", description = "Tìm kiếm giá trị thuộc tính theo từ khóa")
    public ResponseEntity<ApiResponse<List<AttributeValueDto>>> searchAttributeValues(
            @RequestParam String keyword) {

        log.info("API tìm kiếm giá trị thuộc tính với từ khóa: {}", keyword);

        try {
            List<AttributeValueDto> attributeValues = attributeValueService.searchAttributeValues(keyword);
            return ResponseEntity.ok(ApiResponse.success(attributeValues));

        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm giá trị thuộc tính: ", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
