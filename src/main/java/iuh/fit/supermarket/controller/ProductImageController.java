package iuh.fit.supermarket.controller;

import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.dto.product.ProductImageDto;
import iuh.fit.supermarket.dto.product.ProductImageUpdateRequest;
import iuh.fit.supermarket.dto.product.ProductImageUploadRequest;
import iuh.fit.supermarket.dto.product.ProductImageUploadResponse;
import iuh.fit.supermarket.service.ProductImageService;

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
 * REST Controller cho quản lý hình ảnh sản phẩm
 */
@RestController
@RequestMapping("/api/product-images")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ProductImageController {

    private final ProductImageService productImageService;

    /**
     * Upload và lưu hình ảnh mới cho sản phẩm
     * 
     * @param productId ID của sản phẩm
     * @param imageFile File hình ảnh
     * @param imageAlt  Văn bản thay thế (tùy chọn)
     * @param sortOrder Thứ tự sắp xếp (tùy chọn)
     * @return Thông tin hình ảnh đã upload
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ProductImageUploadResponse>> uploadProductImage(
            @RequestParam("productId") @NotNull @Positive Long productId,
            @RequestParam("imageFile") @NotNull MultipartFile imageFile,
            @RequestParam(value = "imageAlt", required = false) String imageAlt,
            @RequestParam(value = "sortOrder", required = false) Integer sortOrder) {

        try {
            log.info("Received request to upload image for product ID: {}", productId);

            // Validate file
            if (imageFile.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File ảnh không được để trống"));
            }

            // Create upload request
            ProductImageUploadRequest request = new ProductImageUploadRequest();
            request.setProductId(productId);
            request.setImageFile(imageFile);
            request.setImageAlt(imageAlt);
            request.setSortOrder(sortOrder);

            // Upload image
            ProductImageUploadResponse response = productImageService.uploadProductImage(request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Upload ảnh thành công", response));

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request for uploading image: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Dữ liệu không hợp lệ: " + e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Error uploading image for product ID: {}", productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi upload ảnh: " + e.getMessage()));
        }
    }

    /**
     * Upload nhiều hình ảnh cho sản phẩm
     * 
     * @param productId ID của sản phẩm
     * @param files     Danh sách file hình ảnh
     * @return Danh sách thông tin hình ảnh đã upload
     */
    @PostMapping("/multiple")
    public ResponseEntity<ApiResponse<List<ProductImageUploadResponse>>> uploadMultipleImages(
            @RequestParam("productId") @NotNull @Positive Long productId,
            @RequestParam("files") @NotNull List<MultipartFile> files) {

        try {
            log.info("Received request to upload {} images for product ID: {}", files.size(), productId);

            if (files.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Danh sách file không được để trống"));
            }

            List<ProductImageUploadResponse> responses = productImageService.uploadMultipleImages(productId, null,
                    files);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(
                            String.format("Upload thành công %d/%d ảnh", responses.size(), files.size()),
                            responses));

        } catch (Exception e) {
            log.error("Error uploading multiple images for product ID: {}", productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi upload nhiều ảnh: " + e.getMessage()));
        }
    }

    /**
     * Lấy tất cả hình ảnh của một sản phẩm cụ thể
     * 
     * @param productId ID của sản phẩm
     * @return Danh sách hình ảnh của sản phẩm
     */
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<List<ProductImageDto>>> getProductImages(
            @PathVariable @NotNull @Positive Long productId) {

        try {
            log.info("Received request to get images for product ID: {}", productId);

            List<ProductImageDto> images = productImageService.getProductImages(productId);

            return ResponseEntity.ok(
                    ApiResponse.success(
                            String.format("Tìm thấy %d ảnh cho sản phẩm", images.size()),
                            images));

        } catch (Exception e) {
            log.error("Error getting images for product ID: {}", productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy danh sách ảnh: " + e.getMessage()));
        }
    }

    /**
     * Lấy thông tin hình ảnh cụ thể theo ID
     *
     * @param imageId ID của hình ảnh
     * @return Thông tin hình ảnh
     */
    @GetMapping("/image/{imageId}")
    public ResponseEntity<ApiResponse<ProductImageDto>> getImageById(
            @PathVariable @NotNull @Positive Integer imageId) {

        try {
            log.info("Received request to get image by ID: {}", imageId);

            ProductImageDto image = productImageService.getImageById(imageId);

            if (image == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy ảnh với ID: " + imageId));
            }

            return ResponseEntity.ok(
                    ApiResponse.success("Lấy thông tin ảnh thành công", image));

        } catch (Exception e) {
            log.error("Error getting image by ID: {}", imageId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy thông tin ảnh: " + e.getMessage()));
        }
    }

    /**
     * Lấy hình ảnh chính của sản phẩm
     * 
     * @param productId ID của sản phẩm
     * @return Hình ảnh chính của sản phẩm
     */
    @GetMapping("/{productId}/main")
    public ResponseEntity<ApiResponse<ProductImageDto>> getMainProductImage(
            @PathVariable @NotNull @Positive Long productId) {

        try {
            log.info("Received request to get main image for product ID: {}", productId);

            ProductImageDto mainImage = productImageService.getMainProductImage(productId);

            if (mainImage == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy ảnh chính cho sản phẩm"));
            }

            return ResponseEntity.ok(
                    ApiResponse.success("Lấy ảnh chính thành công", mainImage));

        } catch (Exception e) {
            log.error("Error getting main image for product ID: {}", productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi lấy ảnh chính: " + e.getMessage()));
        }
    }

    /**
     * Cập nhật thông tin metadata của hình ảnh
     * 
     * @param imageId       ID của hình ảnh
     * @param updateRequest Thông tin cập nhật
     * @return Thông tin hình ảnh đã cập nhật
     */
    @PutMapping("/{imageId}")
    public ResponseEntity<ApiResponse<ProductImageDto>> updateImageMetadata(
            @PathVariable @NotNull @Positive Integer imageId,
            @RequestBody @Valid ProductImageUpdateRequest updateRequest) {

        try {
            log.info("Received request to update image metadata for ID: {}", imageId);

            ProductImageDto updatedImage = null;

            // Update image alt if provided
            if (updateRequest.getImageAlt() != null) {
                updatedImage = productImageService.updateImageAlt(imageId, updateRequest.getImageAlt());
            }

            // Update sort order if provided
            if (updateRequest.getSortOrder() != null) {
                updatedImage = productImageService.updateImageSortOrder(imageId, updateRequest.getSortOrder());
            }

            if (updatedImage == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Không có thông tin nào để cập nhật"));
            }

            return ResponseEntity.ok(
                    ApiResponse.success("Cập nhật thông tin ảnh thành công", updatedImage));

        } catch (RuntimeException e) {
            log.error("Error updating image metadata for ID: {}", imageId, e);
            if (e.getMessage().contains("Không tìm thấy")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error(e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi cập nhật ảnh: " + e.getMessage()));
        }
    }

    /**
     * Xóa một hình ảnh cụ thể
     * 
     * @param imageId ID của hình ảnh cần xóa
     * @return Kết quả xóa
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<ApiResponse<String>> deleteImage(
            @PathVariable @NotNull @Positive Integer imageId) {

        try {
            log.info("Received request to delete image ID: {}", imageId);

            boolean deleted = productImageService.deleteProductImage(imageId);

            if (deleted) {
                return ResponseEntity.ok(
                        ApiResponse.success("Xóa ảnh thành công", "Deleted"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("Không tìm thấy ảnh để xóa"));
            }

        } catch (Exception e) {
            log.error("Error deleting image ID: {}", imageId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi xóa ảnh: " + e.getMessage()));
        }
    }

    /**
     * Xóa tất cả hình ảnh của một sản phẩm
     * 
     * @param productId ID của sản phẩm
     * @return Số lượng ảnh đã xóa
     */
    @DeleteMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<String>> deleteAllProductImages(
            @PathVariable @NotNull @Positive Long productId) {

        try {
            log.info("Received request to delete all images for product ID: {}", productId);

            int deletedCount = productImageService.deleteAllProductImages(productId);

            return ResponseEntity.ok(
                    ApiResponse.success(
                            String.format("Đã xóa %d ảnh của sản phẩm", deletedCount),
                            String.valueOf(deletedCount)));

        } catch (Exception e) {
            log.error("Error deleting all images for product ID: {}", productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi xóa tất cả ảnh: " + e.getMessage()));
        }
    }

    /**
     * Lấy số lượng hình ảnh của sản phẩm
     * 
     * @param productId ID của sản phẩm
     * @return Số lượng hình ảnh
     */
    @GetMapping("/{productId}/count")
    public ResponseEntity<ApiResponse<Long>> getImageCount(
            @PathVariable @NotNull @Positive Long productId) {

        try {
            log.info("Received request to count images for product ID: {}", productId);

            long count = productImageService.countProductImages(productId);

            return ResponseEntity.ok(
                    ApiResponse.success(
                            String.format("Sản phẩm có %d ảnh", count),
                            count));

        } catch (Exception e) {
            log.error("Error counting images for product ID: {}", productId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Lỗi khi đếm số lượng ảnh: " + e.getMessage()));
        }
    }
}
