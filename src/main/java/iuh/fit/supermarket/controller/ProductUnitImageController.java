package iuh.fit.supermarket.controller;

import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.dto.product.*;
import iuh.fit.supermarket.service.ProductUnitImageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

/**
 * REST Controller cho quản lý ảnh của đơn vị sản phẩm
 * Cung cấp các API để gán ảnh từ sản phẩm gốc, upload ảnh mới và quản lý ảnh chính
 */
@RestController
@RequestMapping("/api/product-unit-images")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ProductUnitImageController {

    private final ProductUnitImageService productUnitImageService;

    /**
     * Gán ảnh từ sản phẩm gốc vào đơn vị sản phẩm
     * 
     * @param request Yêu cầu gán ảnh
     * @return Danh sách ảnh của đơn vị sản phẩm sau khi gán
     */
    @PostMapping("/assign")
    public ResponseEntity<ApiResponse<ProductUnitImageResponse>> assignImagesFromProduct(
            @Valid @RequestBody ProductUnitImageAssignRequest request) {
        
        log.info("Nhận yêu cầu gán ảnh cho ProductUnit: {}", request.getProductUnitId());
        
        try {
            ProductUnitImageResponse response = productUnitImageService.assignImagesFromProduct(request);
            
            return ResponseEntity.ok(ApiResponse.<ProductUnitImageResponse>builder()
                    .success(true)
                    .message("Gán ảnh thành công")
                    .data(response)
                    .build());
                    
        } catch (RuntimeException e) {
            log.error("Lỗi khi gán ảnh cho ProductUnit {}: {}", request.getProductUnitId(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<ProductUnitImageResponse>builder()
                            .success(false)
                            .message("Lỗi khi gán ảnh: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Upload ảnh mới và gán trực tiếp vào đơn vị sản phẩm
     * 
     * @param productUnitId ID của đơn vị sản phẩm
     * @param imageFile File ảnh cần upload
     * @param imageAlt Văn bản thay thế cho ảnh (tùy chọn)
     * @param displayOrder Thứ tự hiển thị (tùy chọn)
     * @param isPrimary Có phải ảnh chính không (tùy chọn)
     * @return Thông tin ảnh đã được upload và gán
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductUnitImageDto>> uploadNewImage(
            @RequestParam("productUnitId") @NotNull @Positive Long productUnitId,
            @RequestParam("imageFile") @NotNull MultipartFile imageFile,
            @RequestParam(value = "imageAlt", required = false) String imageAlt,
            @RequestParam(value = "displayOrder", required = false) Integer displayOrder,
            @RequestParam(value = "isPrimary", required = false, defaultValue = "false") Boolean isPrimary) {
        
        log.info("Nhận yêu cầu upload ảnh mới cho ProductUnit: {}", productUnitId);
        
        try {
            ProductUnitImageUploadRequest request = new ProductUnitImageUploadRequest();
            request.setProductUnitId(productUnitId);
            request.setImageFile(imageFile);
            request.setImageAlt(imageAlt);
            request.setDisplayOrder(displayOrder);
            request.setIsPrimary(isPrimary);
            
            ProductUnitImageDto response = productUnitImageService.uploadNewImageForProductUnit(request);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<ProductUnitImageDto>builder()
                            .success(true)
                            .message("Upload ảnh thành công")
                            .data(response)
                            .build());
                            
        } catch (IllegalArgumentException e) {
            log.error("Lỗi validation khi upload ảnh cho ProductUnit {}: {}", productUnitId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<ProductUnitImageDto>builder()
                            .success(false)
                            .message("Lỗi validation: " + e.getMessage())
                            .build());
        } catch (RuntimeException e) {
            log.error("Lỗi khi upload ảnh cho ProductUnit {}: {}", productUnitId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<ProductUnitImageDto>builder()
                            .success(false)
                            .message("Lỗi khi upload ảnh: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Thay đổi ảnh chính của đơn vị sản phẩm
     * 
     * @param request Yêu cầu thay đổi ảnh chính
     * @return Danh sách ảnh của đơn vị sản phẩm với ảnh chính mới
     */
    @PutMapping("/primary")
    public ResponseEntity<ApiResponse<ProductUnitImageResponse>> updatePrimaryImage(
            @Valid @RequestBody ProductUnitImagePrimaryUpdateRequest request) {
        
        log.info("Nhận yêu cầu thay đổi ảnh chính cho ProductUnit: {}", request.getProductUnitId());
        
        try {
            ProductUnitImageResponse response = productUnitImageService.updatePrimaryImage(request);
            
            return ResponseEntity.ok(ApiResponse.<ProductUnitImageResponse>builder()
                    .success(true)
                    .message("Thay đổi ảnh chính thành công")
                    .data(response)
                    .build());
                    
        } catch (RuntimeException e) {
            log.error("Lỗi khi thay đổi ảnh chính cho ProductUnit {}: {}", 
                    request.getProductUnitId(), e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<ProductUnitImageResponse>builder()
                            .success(false)
                            .message("Lỗi khi thay đổi ảnh chính: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Lấy danh sách ảnh của đơn vị sản phẩm
     * 
     * @param productUnitId ID của đơn vị sản phẩm
     * @return Danh sách ảnh của đơn vị sản phẩm
     */
    @GetMapping("/{productUnitId}")
    public ResponseEntity<ApiResponse<ProductUnitImageResponse>> getProductUnitImages(
            @PathVariable @Positive Long productUnitId) {
        
        log.info("Nhận yêu cầu lấy danh sách ảnh cho ProductUnit: {}", productUnitId);
        
        try {
            ProductUnitImageResponse response = productUnitImageService.getProductUnitImages(productUnitId);
            
            return ResponseEntity.ok(ApiResponse.<ProductUnitImageResponse>builder()
                    .success(true)
                    .message("Lấy danh sách ảnh thành công")
                    .data(response)
                    .build());
                    
        } catch (RuntimeException e) {
            log.error("Lỗi khi lấy danh sách ảnh cho ProductUnit {}: {}", productUnitId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<ProductUnitImageResponse>builder()
                            .success(false)
                            .message("Lỗi khi lấy danh sách ảnh: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Lấy danh sách ảnh có sẵn từ sản phẩm gốc để gán
     * 
     * @param productUnitId ID của đơn vị sản phẩm
     * @return Danh sách ảnh có thể gán từ sản phẩm gốc
     */
    @GetMapping("/{productUnitId}/available")
    public ResponseEntity<ApiResponse<List<ProductImageDto>>> getAvailableImages(
            @PathVariable @Positive Long productUnitId) {
        
        log.info("Nhận yêu cầu lấy danh sách ảnh có sẵn cho ProductUnit: {}", productUnitId);
        
        try {
            List<ProductImageDto> availableImages = productUnitImageService.getAvailableProductImages(productUnitId);
            
            return ResponseEntity.ok(ApiResponse.<List<ProductImageDto>>builder()
                    .success(true)
                    .message("Lấy danh sách ảnh có sẵn thành công")
                    .data(availableImages)
                    .build());
                    
        } catch (RuntimeException e) {
            log.error("Lỗi khi lấy danh sách ảnh có sẵn cho ProductUnit {}: {}", productUnitId, e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.<List<ProductImageDto>>builder()
                            .success(false)
                            .message("Lỗi khi lấy danh sách ảnh có sẵn: " + e.getMessage())
                            .build());
        }
    }
}
