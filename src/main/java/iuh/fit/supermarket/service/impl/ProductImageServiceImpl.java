package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.product.ProductImageDto;
import iuh.fit.supermarket.dto.product.ProductImageUploadRequest;
import iuh.fit.supermarket.dto.product.ProductImageUploadResponse;
import iuh.fit.supermarket.entity.Product;
import iuh.fit.supermarket.entity.ProductImage;
import iuh.fit.supermarket.repository.ProductImageRepository;
import iuh.fit.supermarket.repository.ProductRepository;
import iuh.fit.supermarket.service.ProductImageService;
import iuh.fit.supermarket.service.S3FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation cho ProductImageService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final S3FileUploadService s3FileUploadService;

    @Value("${product.image.max-count:5}")
    private int maxImageCount;

    /**
     * Upload hình ảnh cho sản phẩm
     */
    @Override
    public ProductImageUploadResponse uploadProductImage(ProductImageUploadRequest request) {
        log.info("Bắt đầu upload ảnh cho sản phẩm ID: {}", request.getProductId());

        // Kiểm tra sản phẩm có tồn tại không
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + request.getProductId()));

        // Kiểm tra số lượng ảnh hiện tại
        long currentImageCount = productImageRepository.countByProductId(request.getProductId());
        if (currentImageCount >= maxImageCount) {
            throw new RuntimeException("Đã đạt giới hạn số lượng ảnh cho sản phẩm (" + maxImageCount + " ảnh)");
        }

        // Validate file
        if (!s3FileUploadService.isValidFile(request.getImageFile())) {
            throw new IllegalArgumentException("File ảnh không hợp lệ");
        }

        try {
            // Upload file lên S3
            String imageUrl = s3FileUploadService.uploadFile(request.getImageFile(), "products");

            // Tạo entity ProductImage
            ProductImage productImage = new ProductImage();
            productImage.setImageUrl(imageUrl);
            productImage.setImageAlt(request.getImageAlt());
            productImage.setProduct(product);

            // Xử lý variant nếu có
            if (request.getVariantId() != null) {
                // TODO: Tìm variant và set vào productImage
                // ProductVariant variant =
                // productVariantRepository.findById(request.getVariantId())
                // .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể với ID: " +
                // request.getVariantId()));
                // productImage.setVariant(variant);
            }

            // Xử lý sort order
            Integer sortOrder = request.getSortOrder();
            if (sortOrder == null) {
                // Tự động gán sort order tiếp theo
                Integer maxSortOrder = productImageRepository.findMaxSortOrderByProductId(request.getProductId());
                sortOrder = maxSortOrder + 1;
            }
            productImage.setSortOrder(sortOrder);

            // Lưu vào database
            productImage = productImageRepository.save(productImage);

            log.info("Upload ảnh thành công cho sản phẩm ID: {}, Image ID: {}", request.getProductId(),
                    productImage.getImageId());

            return new ProductImageUploadResponse(
                    productImage.getImageId(),
                    productImage.getImageUrl(),
                    "Upload ảnh thành công",
                    productImage.getSortOrder(),
                    request.getImageFile().getSize(),
                    request.getImageFile().getContentType());

        } catch (Exception e) {
            log.error("Lỗi khi upload ảnh cho sản phẩm ID: {}", request.getProductId(), e);
            throw new RuntimeException("Upload ảnh thất bại: " + e.getMessage());
        }
    }

    /**
     * Upload nhiều hình ảnh cho sản phẩm
     */
    @Override
    public List<ProductImageUploadResponse> uploadMultipleImages(Long productId, Long variantId,
            List<MultipartFile> files) {
        log.info("Bắt đầu upload {} ảnh cho sản phẩm ID: {}", files.size(), productId);

        List<ProductImageUploadResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                ProductImageUploadRequest request = new ProductImageUploadRequest();
                request.setProductId(productId);
                request.setVariantId(variantId);
                request.setImageFile(file);

                ProductImageUploadResponse response = uploadProductImage(request);
                responses.add(response);

            } catch (Exception e) {
                log.error("Lỗi khi upload file: {}", file.getOriginalFilename(), e);
                // Tiếp tục upload các file khác
            }
        }

        log.info("Hoàn thành upload multiple images, thành công: {}/{}", responses.size(), files.size());
        return responses;
    }

    /**
     * Lấy tất cả hình ảnh của sản phẩm
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductImageDto> getProductImages(Long productId) {
        log.info("Lấy danh sách ảnh của sản phẩm ID: {}", productId);

        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrder(productId);
        return images.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy hình ảnh của biến thể cụ thể
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductImageDto> getVariantImages(Long variantId) {
        log.info("Lấy danh sách ảnh của biến thể ID: {}", variantId);

        List<ProductImage> images = productImageRepository.findByVariantIdOrderBySortOrder(variantId);
        return images.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Lấy hình ảnh chính của sản phẩm
     */
    @Override
    @Transactional(readOnly = true)
    public ProductImageDto getMainProductImage(Long productId) {
        log.info("Lấy ảnh chính của sản phẩm ID: {}", productId);

        Optional<ProductImage> mainImage = productImageRepository.findMainImageByProductId(productId);
        return mainImage.map(this::mapToDto).orElse(null);
    }

    /**
     * Xóa hình ảnh theo ID
     */
    @Override
    public boolean deleteProductImage(Integer imageId) {
        log.info("Xóa ảnh ID: {}", imageId);

        try {
            Optional<ProductImage> imageOpt = productImageRepository.findById(imageId);
            if (imageOpt.isEmpty()) {
                log.warn("Không tìm thấy ảnh với ID: {}", imageId);
                return false;
            }

            ProductImage image = imageOpt.get();
            String imageUrl = image.getImageUrl();

            // Xóa file từ S3
            boolean deletedFromS3 = s3FileUploadService.deleteFile(imageUrl);
            if (!deletedFromS3) {
                log.warn("Không thể xóa file từ S3: {}", imageUrl);
            }

            // Xóa record từ database
            productImageRepository.delete(image);

            log.info("Xóa ảnh thành công ID: {}", imageId);
            return true;

        } catch (Exception e) {
            log.error("Lỗi khi xóa ảnh ID: {}", imageId, e);
            return false;
        }
    }

    /**
     * Xóa tất cả hình ảnh của sản phẩm
     */
    @Override
    public int deleteAllProductImages(Long productId) {
        log.info("Xóa tất cả ảnh của sản phẩm ID: {}", productId);

        List<ProductImage> images = productImageRepository.findByProductIdOrderBySortOrder(productId);
        int deletedCount = 0;

        for (ProductImage image : images) {
            if (deleteProductImage(image.getImageId())) {
                deletedCount++;
            }
        }

        log.info("Đã xóa {}/{} ảnh của sản phẩm ID: {}", deletedCount, images.size(), productId);
        return deletedCount;
    }

    /**
     * Xóa tất cả hình ảnh của biến thể
     */
    @Override
    public int deleteAllVariantImages(Long variantId) {
        log.info("Xóa tất cả ảnh của biến thể ID: {}", variantId);

        List<ProductImage> images = productImageRepository.findByVariantIdOrderBySortOrder(variantId);
        int deletedCount = 0;

        for (ProductImage image : images) {
            if (deleteProductImage(image.getImageId())) {
                deletedCount++;
            }
        }

        log.info("Đã xóa {}/{} ảnh của biến thể ID: {}", deletedCount, images.size(), variantId);
        return deletedCount;
    }

    /**
     * Cập nhật thứ tự sắp xếp hình ảnh
     */
    @Override
    public ProductImageDto updateImageSortOrder(Integer imageId, Integer newSortOrder) {
        log.info("Cập nhật sort order cho ảnh ID: {}, sort order mới: {}", imageId, newSortOrder);

        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh với ID: " + imageId));

        image.setSortOrder(newSortOrder);
        image = productImageRepository.save(image);

        log.info("Cập nhật sort order thành công cho ảnh ID: {}", imageId);
        return mapToDto(image);
    }

    /**
     * Cập nhật văn bản thay thế cho hình ảnh
     */
    @Override
    public ProductImageDto updateImageAlt(Integer imageId, String imageAlt) {
        log.info("Cập nhật image alt cho ảnh ID: {}", imageId);

        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh với ID: " + imageId));

        image.setImageAlt(imageAlt);
        image = productImageRepository.save(image);

        log.info("Cập nhật image alt thành công cho ảnh ID: {}", imageId);
        return mapToDto(image);
    }

    /**
     * Kiểm tra số lượng hình ảnh của sản phẩm
     */
    @Override
    @Transactional(readOnly = true)
    public long countProductImages(Long productId) {
        return productImageRepository.countByProductId(productId);
    }

    /**
     * Kiểm tra số lượng hình ảnh của biến thể
     */
    @Override
    @Transactional(readOnly = true)
    public long countVariantImages(Long variantId) {
        return productImageRepository.countByVariantId(variantId);
    }

    /**
     * Chuyển đổi Entity sang DTO
     */
    private ProductImageDto mapToDto(ProductImage image) {
        ProductImageDto dto = new ProductImageDto();
        dto.setImageId(image.getImageId());
        dto.setImageUrl(image.getImageUrl());
        dto.setImageAlt(image.getImageAlt());
        dto.setSortOrder(image.getSortOrder());
        dto.setCreatedAt(image.getCreatedAt());
        dto.setProductId(image.getProduct().getId());

        if (image.getVariant() != null) {
            dto.setVariantId(image.getVariant().getVariantId());
        }

        return dto;
    }
}
