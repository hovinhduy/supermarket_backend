package iuh.fit.supermarket.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import iuh.fit.supermarket.dto.common.ApiResponse;
import iuh.fit.supermarket.dto.product.ProductImageDto;
import iuh.fit.supermarket.dto.product.ProductImageUploadRequest;
import iuh.fit.supermarket.dto.product.ProductImageUploadResponse;
import iuh.fit.supermarket.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller xử lý các API liên quan đến quản lý hình ảnh sản phẩm
 */
@RestController
@RequestMapping("/api/product-images")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Image Management", description = "APIs cho quản lý hình ảnh sản phẩm")
@SecurityRequirement(name = "Bearer Authentication")
public class ProductImageController {

        private final ProductImageService productImageService;

        /**
         * Upload hình ảnh cho sản phẩm
         */
        @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Upload hình ảnh sản phẩm", description = "Upload một hình ảnh cho sản phẩm lên AWS S3")
        public ResponseEntity<ApiResponse<ProductImageUploadResponse>> uploadProductImage(
                        @Parameter(description = "ID sản phẩm") @RequestParam Long productId,
                        @Parameter(description = "ID biến thể (tùy chọn)") @RequestParam(required = false) Long variantId,
                        @Parameter(description = "Văn bản thay thế") @RequestParam(required = false) String imageAlt,
                        @Parameter(description = "Thứ tự sắp xếp") @RequestParam(required = false) Integer sortOrder,
                        @Parameter(description = "File hình ảnh") @RequestParam("imageFile") MultipartFile imageFile) {

                log.info("API: Upload ảnh cho sản phẩm ID: {}", productId);

                try {
                        ProductImageUploadRequest request = new ProductImageUploadRequest();
                        request.setProductId(productId);
                        request.setVariantId(variantId);
                        request.setImageAlt(imageAlt);
                        request.setSortOrder(sortOrder);
                        request.setImageFile(imageFile);

                        ProductImageUploadResponse response = productImageService.uploadProductImage(request);

                        return ResponseEntity.ok(
                                        ApiResponse.success("Upload ảnh thành công", response));

                } catch (IllegalArgumentException e) {
                        log.warn("Dữ liệu không hợp lệ khi upload ảnh: {}", e.getMessage());
                        return ResponseEntity.badRequest().body(
                                        ApiResponse.error("Dữ liệu không hợp lệ: " + e.getMessage()));
                } catch (Exception e) {
                        log.error("Lỗi khi upload ảnh sản phẩm", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                        ApiResponse.error("Upload ảnh thất bại: " + e.getMessage()));
                }
        }

        /**
         * Upload nhiều hình ảnh cho sản phẩm
         */
        @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Upload nhiều hình ảnh sản phẩm", description = "Upload nhiều hình ảnh cho sản phẩm cùng lúc")
        public ResponseEntity<ApiResponse<List<ProductImageUploadResponse>>> uploadMultipleImages(
                        @Parameter(description = "ID sản phẩm") @RequestParam Long productId,
                        @Parameter(description = "ID biến thể (tùy chọn)") @RequestParam(required = false) Long variantId,
                        @Parameter(description = "Danh sách file hình ảnh") @RequestParam("imageFiles") List<MultipartFile> imageFiles) {

                log.info("API: Upload {} ảnh cho sản phẩm ID: {}", imageFiles.size(), productId);

                try {
                        List<ProductImageUploadResponse> responses = productImageService.uploadMultipleImages(productId,
                                        variantId,
                                        imageFiles);

                        return ResponseEntity.ok(
                                        ApiResponse.success(String.format("Upload thành công %d/%d ảnh",
                                                        responses.size(), imageFiles.size()), responses));

                } catch (Exception e) {
                        log.error("Lỗi khi upload multiple ảnh sản phẩm", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                        ApiResponse.error("Upload multiple ảnh thất bại: " + e.getMessage()));
                }
        }

        /**
         * Lấy tất cả hình ảnh của sản phẩm
         */
        @GetMapping("/product/{productId}")
        @Operation(summary = "Lấy hình ảnh sản phẩm", description = "Lấy tất cả hình ảnh của một sản phẩm")
        public ResponseEntity<ApiResponse<List<ProductImageDto>>> getProductImages(
                        @Parameter(description = "ID sản phẩm") @PathVariable Long productId) {

                log.info("API: Lấy danh sách ảnh của sản phẩm ID: {}", productId);

                try {
                        List<ProductImageDto> images = productImageService.getProductImages(productId);

                        return ResponseEntity.ok(
                                        ApiResponse.success("Lấy danh sách ảnh thành công", images));

                } catch (Exception e) {
                        log.error("Lỗi khi lấy danh sách ảnh sản phẩm", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                        ApiResponse.error("Lấy danh sách ảnh thất bại: " + e.getMessage()));
                }
        }

        /**
         * Lấy hình ảnh của biến thể cụ thể
         */
        @GetMapping("/variant/{variantId}")
        @Operation(summary = "Lấy hình ảnh biến thể", description = "Lấy tất cả hình ảnh của một biến thể sản phẩm")
        public ResponseEntity<ApiResponse<List<ProductImageDto>>> getVariantImages(
                        @Parameter(description = "ID biến thể") @PathVariable Long variantId) {

                log.info("API: Lấy danh sách ảnh của biến thể ID: {}", variantId);

                try {
                        List<ProductImageDto> images = productImageService.getVariantImages(variantId);

                        return ResponseEntity.ok(
                                        ApiResponse.success("Lấy danh sách ảnh biến thể thành công", images));

                } catch (Exception e) {
                        log.error("Lỗi khi lấy danh sách ảnh biến thể", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                        ApiResponse.error("Lấy danh sách ảnh biến thể thất bại: " + e.getMessage()));
                }
        }

        /**
         * Lấy hình ảnh chính của sản phẩm
         */
        @GetMapping("/product/{productId}/main")
        @Operation(summary = "Lấy hình ảnh chính", description = "Lấy hình ảnh chính của sản phẩm")
        public ResponseEntity<ApiResponse<ProductImageDto>> getMainProductImage(
                        @Parameter(description = "ID sản phẩm") @PathVariable Long productId) {

                log.info("API: Lấy ảnh chính của sản phẩm ID: {}", productId);

                try {
                        ProductImageDto mainImage = productImageService.getMainProductImage(productId);

                        if (mainImage == null) {
                                return ResponseEntity.ok(
                                                ApiResponse.success("Sản phẩm chưa có ảnh chính", null));
                        }

                        return ResponseEntity.ok(
                                        ApiResponse.success("Lấy ảnh chính thành công", mainImage));

                } catch (Exception e) {
                        log.error("Lỗi khi lấy ảnh chính sản phẩm", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                        ApiResponse.error("Lấy ảnh chính thất bại: " + e.getMessage()));
                }
        }

        /**
         * Xóa hình ảnh theo ID
         */
        @DeleteMapping("/{imageId}")
        @Operation(summary = "Xóa hình ảnh", description = "Xóa một hình ảnh sản phẩm theo ID")
        public ResponseEntity<ApiResponse<Boolean>> deleteProductImage(
                        @Parameter(description = "ID hình ảnh") @PathVariable Integer imageId) {

                log.info("API: Xóa ảnh ID: {}", imageId);

                try {
                        boolean deleted = productImageService.deleteProductImage(imageId);

                        if (deleted) {
                                return ResponseEntity.ok(
                                                ApiResponse.success("Xóa ảnh thành công", true));
                        } else {
                                return ResponseEntity.badRequest().body(
                                                ApiResponse.error("Không thể xóa ảnh", false));
                        }

                } catch (Exception e) {
                        log.error("Lỗi khi xóa ảnh", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                        ApiResponse.error("Xóa ảnh thất bại: " + e.getMessage(), false));
                }
        }

        /**
         * Xóa tất cả hình ảnh của sản phẩm
         */
        @DeleteMapping("/product/{productId}")
        @Operation(summary = "Xóa tất cả ảnh sản phẩm", description = "Xóa tất cả hình ảnh của một sản phẩm")
        public ResponseEntity<ApiResponse<Integer>> deleteAllProductImages(
                        @Parameter(description = "ID sản phẩm") @PathVariable Long productId) {

                log.info("API: Xóa tất cả ảnh của sản phẩm ID: {}", productId);

                try {
                        int deletedCount = productImageService.deleteAllProductImages(productId);

                        return ResponseEntity.ok(
                                        ApiResponse.success(String.format("Đã xóa %d ảnh", deletedCount),
                                                        deletedCount));

                } catch (Exception e) {
                        log.error("Lỗi khi xóa tất cả ảnh sản phẩm", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                        ApiResponse.error("Xóa tất cả ảnh thất bại: " + e.getMessage(), 0));
                }
        }

        /**
         * Cập nhật thứ tự sắp xếp hình ảnh
         */
        @PutMapping("/{imageId}/sort-order")
        @Operation(summary = "Cập nhật thứ tự ảnh", description = "Cập nhật thứ tự sắp xếp của hình ảnh")
        public ResponseEntity<ApiResponse<ProductImageDto>> updateImageSortOrder(
                        @Parameter(description = "ID hình ảnh") @PathVariable Integer imageId,
                        @Parameter(description = "Thứ tự mới") @RequestParam Integer sortOrder) {

                log.info("API: Cập nhật sort order cho ảnh ID: {}", imageId);

                try {
                        ProductImageDto updatedImage = productImageService.updateImageSortOrder(imageId, sortOrder);

                        return ResponseEntity.ok(
                                        ApiResponse.success("Cập nhật thứ tự ảnh thành công", updatedImage));

                } catch (Exception e) {
                        log.error("Lỗi khi cập nhật sort order ảnh", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                        ApiResponse.error("Cập nhật thứ tự ảnh thất bại: " + e.getMessage()));
                }
        }

        /**
         * Cập nhật văn bản thay thế cho hình ảnh
         */
        @PutMapping("/{imageId}/alt")
        @Operation(summary = "Cập nhật alt text", description = "Cập nhật văn bản thay thế cho hình ảnh")
        public ResponseEntity<ApiResponse<ProductImageDto>> updateImageAlt(
                        @Parameter(description = "ID hình ảnh") @PathVariable Integer imageId,
                        @Parameter(description = "Văn bản thay thế mới") @RequestParam String imageAlt) {

                log.info("API: Cập nhật alt text cho ảnh ID: {}", imageId);

                try {
                        ProductImageDto updatedImage = productImageService.updateImageAlt(imageId, imageAlt);

                        return ResponseEntity.ok(
                                        ApiResponse.success("Cập nhật alt text thành công", updatedImage));

                } catch (Exception e) {
                        log.error("Lỗi khi cập nhật alt text ảnh", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                        ApiResponse.error("Cập nhật alt text thất bại: " + e.getMessage()));
                }
        }

        /**
         * Xóa tất cả hình ảnh của biến thể
         */
        @DeleteMapping("/variant/{variantId}")
        @Operation(summary = "Xóa tất cả ảnh biến thể", description = "Xóa tất cả hình ảnh của một biến thể sản phẩm")
        public ResponseEntity<ApiResponse<Integer>> deleteAllVariantImages(
                        @Parameter(description = "ID biến thể") @PathVariable Long variantId) {

                log.info("API: Xóa tất cả ảnh của biến thể ID: {}", variantId);

                try {
                        int deletedCount = productImageService.deleteAllVariantImages(variantId);

                        return ResponseEntity.ok(
                                        ApiResponse.success(String.format("Đã xóa %d ảnh của biến thể", deletedCount),
                                                        deletedCount));

                } catch (Exception e) {
                        log.error("Lỗi khi xóa tất cả ảnh biến thể", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                        ApiResponse.error("Xóa tất cả ảnh biến thể thất bại: " + e.getMessage(), 0));
                }
        }

        /**
         * Lấy số lượng hình ảnh của sản phẩm
         */
        @GetMapping("/product/{productId}/count")
        @Operation(summary = "Đếm số ảnh sản phẩm", description = "Lấy số lượng hình ảnh của sản phẩm")
        public ResponseEntity<ApiResponse<Long>> countProductImages(
                        @Parameter(description = "ID sản phẩm") @PathVariable Long productId) {

                log.info("API: Đếm số ảnh của sản phẩm ID: {}", productId);

                try {
                        long count = productImageService.countProductImages(productId);

                        return ResponseEntity.ok(
                                        ApiResponse.success("Lấy số lượng ảnh thành công", count));

                } catch (Exception e) {
                        log.error("Lỗi khi đếm số ảnh sản phẩm", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                        ApiResponse.error("Đếm số ảnh thất bại: " + e.getMessage(), 0L));
                }
        }

        /**
         * Lấy số lượng hình ảnh của biến thể
         */
        @GetMapping("/variant/{variantId}/count")
        @Operation(summary = "Đếm số ảnh biến thể", description = "Lấy số lượng hình ảnh của biến thể")
        public ResponseEntity<ApiResponse<Long>> countVariantImages(
                        @Parameter(description = "ID biến thể") @PathVariable Long variantId) {

                log.info("API: Đếm số ảnh của biến thể ID: {}", variantId);

                try {
                        long count = productImageService.countVariantImages(variantId);

                        return ResponseEntity.ok(
                                        ApiResponse.success("Lấy số lượng ảnh biến thể thành công", count));

                } catch (Exception e) {
                        log.error("Lỗi khi đếm số ảnh biến thể", e);
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                                        ApiResponse.error("Đếm số ảnh biến thể thất bại: " + e.getMessage(), 0L));
                }
        }
}
