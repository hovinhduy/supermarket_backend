package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.product.*;
import iuh.fit.supermarket.entity.ProductUnitImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface quản lý mối quan hệ giữa ProductUnit và ProductImage
 * Xử lý việc chọn và quản lý ảnh cho các ProductUnit cụ thể
 */
public interface ProductUnitImageService {

    /**
     * Add an image to a ProductUnit's selected images
     * Validates that the image belongs to the same product as the ProductUnit
     *
     * @param productUnitId  ID of the ProductUnit
     * @param productImageId ID of the ProductImage to add
     * @param displayOrder   Display order for the image (optional)
     * @param isPrimary      Whether this should be the primary image
     * @return The created ProductUnitImage mapping
     */
    ProductUnitImage addImageToProductUnit(Long productUnitId, Integer productImageId,
            Integer displayOrder, Boolean isPrimary);

    /**
     * Remove an image from a ProductUnit's selected images
     *
     * @param productUnitId  ID of the ProductUnit
     * @param productImageId ID of the ProductImage to remove
     * @return true if removed successfully
     */
    boolean removeImageFromProductUnit(Long productUnitId, Integer productImageId);

    /**
     * Set an image as the primary image for a ProductUnit
     * Automatically unsets any existing primary image
     *
     * @param productUnitId  ID of the ProductUnit
     * @param productImageId ID of the ProductImage to set as primary
     * @return The updated ProductUnitImage mapping
     */
    ProductUnitImage setPrimaryImage(Long productUnitId, Integer productImageId);

    /**
     * Get all selected images for a ProductUnit, ordered by displayOrder
     *
     * @param productUnitId ID of the ProductUnit
     * @return List of selected ProductImages with their display metadata
     */
    List<ProductImageDto> getSelectedImagesForProductUnit(Long productUnitId);

    /**
     * Get available images that can be selected for a ProductUnit
     * Returns images from the parent Product that are not yet selected
     *
     * @param productUnitId ID of the ProductUnit
     * @return List of available ProductImages
     */
    List<ProductImageDto> getAvailableImagesForProductUnit(Long productUnitId);

    /**
     * Get the primary image for a ProductUnit
     *
     * @param productUnitId ID of the ProductUnit
     * @return Primary ProductImage or null if none set
     */
    ProductImageDto getPrimaryImageForProductUnit(Long productUnitId);

    /**
     * Update the display order of images for a ProductUnit
     *
     * @param productUnitId ID of the ProductUnit
     * @param imageOrderMap Map of ProductImage ID to new display order
     * @return List of updated ProductUnitImage mappings
     */
    List<ProductUnitImage> updateImageDisplayOrder(Long productUnitId,
            java.util.Map<Integer, Integer> imageOrderMap);

    /**
     * Copy image selections from one ProductUnit to another
     * Both ProductUnits must belong to the same Product
     *
     * @param sourceProductUnitId Source ProductUnit ID
     * @param targetProductUnitId Target ProductUnit ID
     * @return List of created ProductUnitImage mappings
     */
    List<ProductUnitImage> copyImageSelections(Long sourceProductUnitId, Long targetProductUnitId);

    /**
     * Clear all image selections for a ProductUnit
     *
     * @param productUnitId ID of the ProductUnit
     * @return Number of images removed
     */
    int clearAllImageSelections(Long productUnitId);

    /**
     * Validate that a ProductImage can be selected by a ProductUnit
     * Checks that both belong to the same Product
     *
     * @param productUnitId  ID of the ProductUnit
     * @param productImageId ID of the ProductImage
     * @return true if valid selection
     */
    boolean validateImageSelection(Long productUnitId, Integer productImageId);

    /**
     * Get count of selected images for a ProductUnit
     *
     * @param productUnitId ID of the ProductUnit
     * @return Number of selected images
     */
    long getSelectedImageCount(Long productUnitId);

    /**
     * Check if a ProductUnit has a primary image set
     *
     * @param productUnitId ID of the ProductUnit
     * @return true if primary image exists
     */
    boolean hasPrimaryImage(Long productUnitId);

    /**
     * Bulk add multiple images to a ProductUnit
     *
     * @param productUnitId     ID of the ProductUnit
     * @param productImageIds   List of ProductImage IDs to add
     * @param startDisplayOrder Starting display order (images will be ordered
     *                          sequentially)
     * @return List of created ProductUnitImage mappings
     */
    List<ProductUnitImage> addMultipleImagesToProductUnit(Long productUnitId,
            List<Integer> productImageIds,
            Integer startDisplayOrder);

    /**
     * Get all ProductUnits that use a specific ProductImage
     *
     * @param productImageId ID of the ProductImage
     * @return List of ProductUnit IDs using this image
     */
    List<Long> getProductUnitsUsingImage(Integer productImageId);

    // ===== CÁC PHƯƠNG THỨC MỚI THEO YÊU CẦU =====

    /**
     * Gán nhiều ảnh từ sản phẩm gốc vào đơn vị sản phẩm
     *
     * @param request Yêu cầu gán ảnh
     * @return Phản hồi danh sách ảnh của đơn vị sản phẩm
     */
    ProductUnitImageResponse assignImagesFromProduct(ProductUnitImageAssignRequest request);

    /**
     * Upload ảnh mới và gán trực tiếp vào đơn vị sản phẩm
     *
     * @param request Yêu cầu upload ảnh
     * @return Thông tin ảnh đã được upload và gán
     */
    ProductUnitImageDto uploadNewImageForProductUnit(ProductUnitImageUploadRequest request);

    /**
     * Thay đổi ảnh chính của đơn vị sản phẩm
     *
     * @param request Yêu cầu thay đổi ảnh chính
     * @return Phản hồi danh sách ảnh với ảnh chính mới
     */
    ProductUnitImageResponse updatePrimaryImage(ProductUnitImagePrimaryUpdateRequest request);

    /**
     * Lấy danh sách đầy đủ ảnh của đơn vị sản phẩm
     *
     * @param productUnitId ID của đơn vị sản phẩm
     * @return Phản hồi danh sách ảnh của đơn vị sản phẩm
     */
    ProductUnitImageResponse getProductUnitImages(Long productUnitId);

    /**
     * Lấy danh sách ảnh có sẵn từ sản phẩm gốc để gán
     *
     * @param productUnitId ID của đơn vị sản phẩm
     * @return Danh sách ảnh có thể gán từ sản phẩm gốc
     */
    List<ProductImageDto> getAvailableProductImages(Long productUnitId);
}
