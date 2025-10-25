package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.product.*;
import iuh.fit.supermarket.entity.Product;
import iuh.fit.supermarket.entity.ProductImage;
import iuh.fit.supermarket.entity.ProductUnit;
import iuh.fit.supermarket.entity.ProductUnitImage;
import iuh.fit.supermarket.repository.ProductImageRepository;
import iuh.fit.supermarket.repository.ProductUnitImageRepository;
import iuh.fit.supermarket.repository.ProductUnitRepository;
import iuh.fit.supermarket.service.ProductUnitImageService;
import iuh.fit.supermarket.service.S3FileUploadService;
import iuh.fit.supermarket.util.ProductUnitImageValidator;
import iuh.fit.supermarket.exception.ProductUnitImageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of ProductUnitImageService
 * Manages the relationship between ProductUnits and ProductImages
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductUnitImageServiceImpl implements ProductUnitImageService {

    private final ProductUnitImageRepository productUnitImageRepository;
    private final ProductUnitRepository productUnitRepository;
    private final ProductImageRepository productImageRepository;
    private final S3FileUploadService s3FileUploadService;
    private final ProductUnitImageValidator validator;

    @Override
    public ProductUnitImage addImageToProductUnit(Long productUnitId, Integer productImageId,
            Integer displayOrder, Boolean isPrimary) {
        log.info("Adding image {} to ProductUnit {}", productImageId, productUnitId);

        // Validate the selection
        if (!validateImageSelection(productUnitId, productImageId)) {
            throw new IllegalArgumentException("ProductImage does not belong to the same Product as ProductUnit");
        }

        // Check if mapping already exists
        Optional<ProductUnitImage> existing = productUnitImageRepository
                .findByProductUnitIdAndProductImageId(productUnitId, productImageId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Image is already selected for this ProductUnit");
        }

        // Get entities
        ProductUnit productUnit = productUnitRepository.findById(productUnitId)
                .orElseThrow(() -> new RuntimeException("ProductUnit not found: " + productUnitId));
        ProductImage productImage = productImageRepository.findById(productImageId)
                .orElseThrow(() -> new RuntimeException("ProductImage not found: " + productImageId));

        // Handle primary image logic
        if (Boolean.TRUE.equals(isPrimary)) {
            // Unset existing primary image
            unsetExistingPrimaryImage(productUnitId);
        }

        // Set display order if not provided
        if (displayOrder == null) {
            displayOrder = getNextDisplayOrder(productUnitId);
        }

        // Create mapping
        ProductUnitImage mapping = new ProductUnitImage(productUnit, productImage, displayOrder, isPrimary);
        return productUnitImageRepository.save(mapping);
    }

    @Override
    public boolean removeImageFromProductUnit(Long productUnitId, Integer productImageId) {
        log.info("Removing image {} from ProductUnit {}", productImageId, productUnitId);

        Optional<ProductUnitImage> mapping = productUnitImageRepository
                .findByProductUnitIdAndProductImageId(productUnitId, productImageId);

        if (mapping.isPresent()) {
            ProductUnitImage pui = mapping.get();
            pui.setIsActive(false);
            productUnitImageRepository.save(pui);
            return true;
        }
        return false;
    }

    @Override
    public ProductUnitImage setPrimaryImage(Long productUnitId, Integer productImageId) {
        log.info("Setting primary image {} for ProductUnit {}", productImageId, productUnitId);

        // Validate the selection
        if (!validateImageSelection(productUnitId, productImageId)) {
            throw new IllegalArgumentException("ProductImage does not belong to the same Product as ProductUnit");
        }

        // Unset existing primary image
        unsetExistingPrimaryImage(productUnitId);

        // Get or create the mapping
        Optional<ProductUnitImage> existing = productUnitImageRepository
                .findByProductUnitIdAndProductImageId(productUnitId, productImageId);

        ProductUnitImage mapping;
        if (existing.isPresent()) {
            mapping = existing.get();
            mapping.setIsPrimary(true);
            mapping.setIsActive(true);
        } else {
            // Add the image if not already selected
            mapping = addImageToProductUnit(productUnitId, productImageId, null, true);
        }

        return productUnitImageRepository.save(mapping);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductImageDto> getSelectedImagesForProductUnit(Long productUnitId) {
        List<ProductImage> images = productImageRepository
                .findSelectedImagesForProductUnit(null, productUnitId);
        return images.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductImageDto> getAvailableImagesForProductUnit(Long productUnitId) {
        ProductUnit productUnit = productUnitRepository.findById(productUnitId)
                .orElseThrow(() -> new RuntimeException("ProductUnit not found: " + productUnitId));

        List<ProductImage> images = productImageRepository
                .findAvailableImagesForProductUnit(productUnit.getProduct().getId(), productUnitId);
        return images.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductImageDto getPrimaryImageForProductUnit(Long productUnitId) {
        Optional<ProductUnitImage> primaryMapping = productUnitImageRepository
                .findPrimaryImageByProductUnitId(productUnitId);

        return primaryMapping.map(pui -> convertToDto(pui.getProductImage())).orElse(null);
    }

    @Override
    public List<ProductUnitImage> updateImageDisplayOrder(Long productUnitId, Map<Integer, Integer> imageOrderMap) {
        log.info("Updating display order for ProductUnit {}", productUnitId);

        List<ProductUnitImage> mappings = productUnitImageRepository
                .findByProductUnitIdOrderByDisplayOrder(productUnitId);

        List<ProductUnitImage> updatedMappings = new ArrayList<>();
        for (ProductUnitImage mapping : mappings) {
            Integer newOrder = imageOrderMap.get(mapping.getProductImage().getImageId());
            if (newOrder != null) {
                mapping.setDisplayOrder(newOrder);
                updatedMappings.add(productUnitImageRepository.save(mapping));
            }
        }

        return updatedMappings;
    }

    @Override
    public List<ProductUnitImage> copyImageSelections(Long sourceProductUnitId, Long targetProductUnitId) {
        log.info("Copying image selections from ProductUnit {} to {}", sourceProductUnitId, targetProductUnitId);

        // Validate both ProductUnits belong to same Product
        ProductUnit sourceUnit = productUnitRepository.findById(sourceProductUnitId)
                .orElseThrow(() -> new RuntimeException("Source ProductUnit not found: " + sourceProductUnitId));
        ProductUnit targetUnit = productUnitRepository.findById(targetProductUnitId)
                .orElseThrow(() -> new RuntimeException("Target ProductUnit not found: " + targetProductUnitId));

        if (!sourceUnit.getProduct().getId().equals(targetUnit.getProduct().getId())) {
            throw new IllegalArgumentException("ProductUnits must belong to the same Product");
        }

        // Get source mappings
        List<ProductUnitImage> sourceMappings = productUnitImageRepository
                .findByProductUnitIdOrderByDisplayOrder(sourceProductUnitId);

        // Create new mappings for target
        List<ProductUnitImage> newMappings = new ArrayList<>();
        for (ProductUnitImage sourceMapping : sourceMappings) {
            // Check if mapping already exists
            Optional<ProductUnitImage> existing = productUnitImageRepository
                    .findByProductUnitIdAndProductImageId(targetProductUnitId,
                            sourceMapping.getProductImage().getImageId());

            if (existing.isEmpty()) {
                ProductUnitImage newMapping = new ProductUnitImage(
                        targetUnit,
                        sourceMapping.getProductImage(),
                        sourceMapping.getDisplayOrder(),
                        sourceMapping.getIsPrimary());
                newMappings.add(productUnitImageRepository.save(newMapping));
            }
        }

        return newMappings;
    }

    @Override
    public int clearAllImageSelections(Long productUnitId) {
        log.info("Clearing all image selections for ProductUnit {}", productUnitId);

        List<ProductUnitImage> mappings = productUnitImageRepository
                .findByProductUnitIdOrderByDisplayOrder(productUnitId);

        for (ProductUnitImage mapping : mappings) {
            mapping.setIsActive(false);
            productUnitImageRepository.save(mapping);
        }

        return mappings.size();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateImageSelection(Long productUnitId, Integer productImageId) {
        ProductUnit productUnit = productUnitRepository.findById(productUnitId)
                .orElseThrow(() -> new RuntimeException("ProductUnit not found: " + productUnitId));
        ProductImage productImage = productImageRepository.findById(productImageId)
                .orElseThrow(() -> new RuntimeException("ProductImage not found: " + productImageId));

        return productUnit.getProduct().getId().equals(productImage.getProduct().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public long getSelectedImageCount(Long productUnitId) {
        return productUnitImageRepository.countByProductUnitId(productUnitId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPrimaryImage(Long productUnitId) {
        return productUnitImageRepository.existsPrimaryImageForProductUnit(productUnitId);
    }

    @Override
    public List<ProductUnitImage> addMultipleImagesToProductUnit(Long productUnitId,
            List<Integer> productImageIds,
            Integer startDisplayOrder) {
        log.info("Adding multiple images to ProductUnit {}", productUnitId);

        List<ProductUnitImage> newMappings = new ArrayList<>();
        int currentOrder = startDisplayOrder != null ? startDisplayOrder : getNextDisplayOrder(productUnitId);

        for (Integer imageId : productImageIds) {
            try {
                ProductUnitImage mapping = addImageToProductUnit(productUnitId, imageId, currentOrder, false);
                newMappings.add(mapping);
                currentOrder++;
            } catch (IllegalArgumentException e) {
                log.warn("Skipping image {}: {}", imageId, e.getMessage());
            }
        }

        return newMappings;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getProductUnitsUsingImage(Integer productImageId) {
        List<ProductUnitImage> mappings = productUnitImageRepository.findByProductImageId(productImageId);
        return mappings.stream()
                .map(pui -> pui.getProductUnit().getId())
                .collect(Collectors.toList());
    }

    // Helper methods
    private void unsetExistingPrimaryImage(Long productUnitId) {
        Optional<ProductUnitImage> existingPrimary = productUnitImageRepository
                .findPrimaryImageByProductUnitId(productUnitId);
        if (existingPrimary.isPresent()) {
            ProductUnitImage existing = existingPrimary.get();
            existing.setIsPrimary(false);
            productUnitImageRepository.save(existing);
        }
    }

    private Integer getNextDisplayOrder(Long productUnitId) {
        List<ProductUnitImage> mappings = productUnitImageRepository
                .findByProductUnitIdOrderByDisplayOrder(productUnitId);
        return mappings.isEmpty() ? 0 : mappings.get(mappings.size() - 1).getDisplayOrder() + 1;
    }

    private ProductImageDto convertToDto(ProductImage productImage) {
        ProductImageDto dto = new ProductImageDto();
        dto.setImageId(productImage.getImageId());
        dto.setImageUrl(productImage.getImageUrl());
        dto.setImageAlt(productImage.getImageAlt());
        dto.setSortOrder(productImage.getSortOrder());
        dto.setCreatedAt(productImage.getCreatedAt());
        dto.setProductId(productImage.getProduct().getId());
        return dto;
    }

    // ===== TRIỂN KHAI CÁC PHƯƠNG THỨC MỚI =====

    @Override
    public ProductUnitImageResponse assignImagesFromProduct(ProductUnitImageAssignRequest request) {
        log.info("Gán ảnh từ sản phẩm gốc cho ProductUnit {}", request.getProductUnitId());

        // Validate request
        validator.validateAssignRequest(request);
        validator.validateBusinessRules(request.getProductUnitId(), request.getProductImageIds());

        List<ProductUnitImage> newMappings = new ArrayList<>();

        // Gán từng ảnh
        for (Integer imageId : request.getProductImageIds()) {
            try {
                boolean isPrimary = imageId.equals(request.getPrimaryImageId());
                ProductUnitImage mapping = addImageToProductUnit(
                        request.getProductUnitId(),
                        imageId,
                        null,
                        isPrimary);
                newMappings.add(mapping);
            } catch (IllegalArgumentException e) {
                log.warn("Bỏ qua ảnh {}: {}", imageId, e.getMessage());
            }
        }

        return getProductUnitImages(request.getProductUnitId());
    }

    @Override
    public ProductUnitImageDto uploadNewImageForProductUnit(ProductUnitImageUploadRequest request) {
        log.info("Upload ảnh mới cho ProductUnit {}", request.getProductUnitId());

        // Validate request
        validator.validateUploadRequest(request);

        // Get ProductUnit
        ProductUnit productUnit = validator.validateProductUnitExists(request.getProductUnitId());

        // Upload file to S3
        String imageUrl = s3FileUploadService.uploadFile(request.getImageFile(), "product-unit-images");

        // Create new ProductImage for the product
        ProductImage productImage = new ProductImage();
        productImage.setImageUrl(imageUrl);
        productImage.setImageAlt(request.getImageAlt());
        productImage.setSortOrder(0);
        productImage.setProduct(productUnit.getProduct());
        productImage = productImageRepository.save(productImage);

        // Handle primary image logic
        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            unsetExistingPrimaryImage(request.getProductUnitId());
        }

        // Create mapping
        ProductUnitImage mapping = new ProductUnitImage(
                productUnit,
                productImage,
                request.getDisplayOrder() != null ? request.getDisplayOrder()
                        : getNextDisplayOrder(request.getProductUnitId()),
                request.getIsPrimary());
        mapping = productUnitImageRepository.save(mapping);

        // Convert to DTO
        ProductUnitImageDto dto = new ProductUnitImageDto();
        dto.setId(mapping.getId());
        dto.setDisplayOrder(mapping.getDisplayOrder());
        dto.setIsPrimary(mapping.getIsPrimary());
        dto.setIsActive(mapping.getIsActive());
        dto.setCreatedAt(mapping.getCreatedAt());
        dto.setProductUnitId(mapping.getProductUnit().getId());
        dto.setProductImage(convertToDto(mapping.getProductImage()));

        return dto;
    }

    @Override
    public ProductUnitImageResponse updatePrimaryImage(ProductUnitImagePrimaryUpdateRequest request) {
        log.info("Thay đổi ảnh chính cho ProductUnit {} thành ảnh {}",
                request.getProductUnitId(), request.getProductImageId());

        setPrimaryImage(request.getProductUnitId(), request.getProductImageId());
        return getProductUnitImages(request.getProductUnitId());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductUnitImageResponse getProductUnitImages(Long productUnitId) {
        log.info("Lấy danh sách ảnh cho ProductUnit {}", productUnitId);

        ProductUnit productUnit = productUnitRepository.findById(productUnitId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn vị sản phẩm: " + productUnitId));

        List<ProductUnitImage> mappings = productUnitImageRepository
                .findByProductUnitIdOrderByDisplayOrder(productUnitId);

        List<ProductUnitImageDto> imageDtos = new ArrayList<>();
        ProductUnitImageDto primaryImage = null;

        for (ProductUnitImage mapping : mappings) {
            if (Boolean.TRUE.equals(mapping.getIsActive())) {
                ProductUnitImageDto dto = new ProductUnitImageDto();
                dto.setId(mapping.getId());
                dto.setDisplayOrder(mapping.getDisplayOrder());
                dto.setIsPrimary(mapping.getIsPrimary());
                dto.setIsActive(mapping.getIsActive());
                dto.setCreatedAt(mapping.getCreatedAt());
                dto.setProductUnitId(mapping.getProductUnit().getId());
                dto.setProductImage(convertToDto(mapping.getProductImage()));

                imageDtos.add(dto);

                if (Boolean.TRUE.equals(mapping.getIsPrimary())) {
                    primaryImage = dto;
                }
            }
        }

        ProductUnitImageResponse response = new ProductUnitImageResponse();
        response.setProductUnitId(productUnitId);
        response.setProductUnitName(String.valueOf(productUnit.getId())); // Sử dụng ID làm tên
        response.setImages(imageDtos);
        response.setPrimaryImage(primaryImage);
        response.setTotalImages(imageDtos.size());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductImageDto> getAvailableProductImages(Long productUnitId) {
        log.info("Lấy danh sách ảnh có sẵn cho ProductUnit {}", productUnitId);
        return getAvailableImagesForProductUnit(productUnitId);
    }
}
