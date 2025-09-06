package iuh.fit.supermarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.dto.product.AttributeCreateRequest;
import iuh.fit.supermarket.dto.product.AttributeDto;
import iuh.fit.supermarket.service.AttributeService;
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
}
