package iuh.fit.supermarket.util;

import iuh.fit.supermarket.dto.product.ProductUnitImageAssignRequest;
import iuh.fit.supermarket.dto.product.ProductUnitImageUploadRequest;
import iuh.fit.supermarket.entity.ProductImage;
import iuh.fit.supermarket.entity.ProductUnit;
import iuh.fit.supermarket.exception.ProductUnitImageException;
import iuh.fit.supermarket.repository.ProductImageRepository;
import iuh.fit.supermarket.repository.ProductUnitImageRepository;
import iuh.fit.supermarket.repository.ProductUnitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Validator class cho hệ thống quản lý ảnh đơn vị sản phẩm
 * Đảm bảo tính toàn vẹn dữ liệu và validation logic
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductUnitImageValidator {

    private final ProductUnitRepository productUnitRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductUnitImageRepository productUnitImageRepository;

    // Các định dạng file ảnh được hỗ trợ
    private static final List<String> SUPPORTED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    // Kích thước file tối đa (5MB)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * Validate yêu cầu gán ảnh từ sản phẩm gốc
     */
    public void validateAssignRequest(ProductUnitImageAssignRequest request) {
        log.debug("Validating assign request for ProductUnit: {}", request.getProductUnitId());

        // Validate ProductUnit exists
        ProductUnit productUnit = validateProductUnitExists(request.getProductUnitId());

        // Validate danh sách ảnh không rỗng
        if (request.getProductImageIds() == null || request.getProductImageIds().isEmpty()) {
            throw new IllegalArgumentException("Danh sách ID ảnh không được để trống");
        }

        // Validate từng ảnh
        for (Integer imageId : request.getProductImageIds()) {
            validateProductImageExists(imageId);
            validateImageBelongsToProduct(imageId, productUnit.getProduct().getId());
            
            // Kiểm tra ảnh đã được gán chưa
            if (productUnitImageRepository.findByProductUnitIdAndProductImageId(
                    request.getProductUnitId(), imageId).isPresent()) {
                log.warn("Ảnh {} đã được gán cho ProductUnit {}", imageId, request.getProductUnitId());
                // Không throw exception, chỉ log warning để cho phép gán lại
            }
        }

        // Validate primary image nếu có
        if (request.getPrimaryImageId() != null) {
            if (!request.getProductImageIds().contains(request.getPrimaryImageId())) {
                throw new IllegalArgumentException("Ảnh chính phải nằm trong danh sách ảnh được gán");
            }
        }
    }

    /**
     * Validate yêu cầu upload ảnh mới
     */
    public void validateUploadRequest(ProductUnitImageUploadRequest request) {
        log.debug("Validating upload request for ProductUnit: {}", request.getProductUnitId());

        // Validate ProductUnit exists
        validateProductUnitExists(request.getProductUnitId());

        // Validate file
        validateImageFile(request.getImageFile());

        // Validate display order
        if (request.getDisplayOrder() != null && request.getDisplayOrder() < 0) {
            throw new IllegalArgumentException("Thứ tự hiển thị phải là số không âm");
        }
    }

    /**
     * Validate file ảnh
     */
    public void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ProductUnitImageException.InvalidImageFileException("File không được để trống");
        }

        // Kiểm tra kích thước file
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ProductUnitImageException.InvalidImageFileException(
                    String.format("Kích thước file vượt quá giới hạn %d MB", MAX_FILE_SIZE / (1024 * 1024))
            );
        }

        // Kiểm tra định dạng file
        String contentType = file.getContentType();
        if (contentType == null || !SUPPORTED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new ProductUnitImageException.InvalidImageFileException(
                    "Định dạng file không được hỗ trợ. Chỉ chấp nhận: " + String.join(", ", SUPPORTED_IMAGE_TYPES)
            );
        }

        // Kiểm tra tên file
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new ProductUnitImageException.InvalidImageFileException("Tên file không hợp lệ");
        }
    }

    /**
     * Validate ProductUnit tồn tại
     */
    public ProductUnit validateProductUnitExists(Long productUnitId) {
        return productUnitRepository.findById(productUnitId)
                .orElseThrow(() -> new ProductUnitImageException.ProductUnitNotFoundException(productUnitId));
    }

    /**
     * Validate ProductImage tồn tại
     */
    public ProductImage validateProductImageExists(Integer productImageId) {
        return productImageRepository.findById(productImageId)
                .orElseThrow(() -> new ProductUnitImageException.ProductImageNotFoundException(productImageId));
    }

    /**
     * Validate ảnh thuộc về sản phẩm
     */
    public void validateImageBelongsToProduct(Integer imageId, Long productId) {
        ProductImage image = validateProductImageExists(imageId);
        if (!image.getProduct().getId().equals(productId)) {
            throw new ProductUnitImageException.ImageProductMismatchException(imageId, productId);
        }
    }

    /**
     * Validate có thể đặt ảnh làm ảnh chính
     */
    public void validateCanSetPrimaryImage(Long productUnitId, Integer imageId) {
        // Kiểm tra ProductUnit tồn tại
        ProductUnit productUnit = validateProductUnitExists(productUnitId);
        
        // Kiểm tra ảnh tồn tại và thuộc về sản phẩm
        validateImageBelongsToProduct(imageId, productUnit.getProduct().getId());
        
        // Kiểm tra ảnh đã được gán cho ProductUnit chưa
        Optional<iuh.fit.supermarket.entity.ProductUnitImage> mapping = 
                productUnitImageRepository.findByProductUnitIdAndProductImageId(productUnitId, imageId);
        
        if (mapping.isEmpty()) {
            log.info("Ảnh {} chưa được gán cho ProductUnit {}, sẽ tự động gán khi đặt làm ảnh chính", 
                    imageId, productUnitId);
        } else if (!Boolean.TRUE.equals(mapping.get().getIsActive())) {
            throw new IllegalArgumentException("Không thể đặt ảnh đã bị vô hiệu hóa làm ảnh chính");
        }
    }

    /**
     * Validate business rules cho việc gán ảnh
     */
    public void validateBusinessRules(Long productUnitId, List<Integer> imageIds) {
        // Giới hạn số lượng ảnh tối đa cho một ProductUnit
        final int MAX_IMAGES_PER_UNIT = 10;
        
        long currentImageCount = productUnitImageRepository.countByProductUnitId(productUnitId);
        long newImageCount = imageIds.size();
        
        if (currentImageCount + newImageCount > MAX_IMAGES_PER_UNIT) {
            throw new IllegalArgumentException(
                    String.format("Vượt quá giới hạn %d ảnh cho một đơn vị sản phẩm. " +
                            "Hiện tại: %d, thêm mới: %d", 
                            MAX_IMAGES_PER_UNIT, currentImageCount, newImageCount)
            );
        }
    }

    /**
     * Validate có thể xóa ảnh
     */
    public void validateCanRemoveImage(Long productUnitId, Integer imageId) {
        Optional<iuh.fit.supermarket.entity.ProductUnitImage> mapping = 
                productUnitImageRepository.findByProductUnitIdAndProductImageId(productUnitId, imageId);
        
        if (mapping.isEmpty()) {
            throw new IllegalArgumentException("Ảnh không được gán cho đơn vị sản phẩm này");
        }

        // Kiểm tra nếu đây là ảnh chính duy nhất
        if (Boolean.TRUE.equals(mapping.get().getIsPrimary())) {
            long totalActiveImages = productUnitImageRepository.countByProductUnitId(productUnitId);
            if (totalActiveImages == 1) {
                throw new ProductUnitImageException.CannotRemovePrimaryImageException();
            }
        }
    }
}
