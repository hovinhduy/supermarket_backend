package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.product.ProductImageDto;
import iuh.fit.supermarket.dto.product.ProductImageUploadRequest;
import iuh.fit.supermarket.dto.product.ProductImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface cho quản lý hình ảnh sản phẩm
 */
public interface ProductImageService {

    /**
     * Upload hình ảnh cho sản phẩm
     * 
     * @param request thông tin upload ảnh
     * @return thông tin ảnh đã upload
     */
    ProductImageUploadResponse uploadProductImage(ProductImageUploadRequest request);

    /**
     * Upload nhiều hình ảnh cho sản phẩm
     * 
     * @param productId ID sản phẩm
     * @param variantId ID biến thể (tùy chọn)
     * @param files     danh sách file ảnh
     * @return danh sách thông tin ảnh đã upload
     */
    List<ProductImageUploadResponse> uploadMultipleImages(Long productId, Integer variantId, List<MultipartFile> files);

    /**
     * Lấy tất cả hình ảnh của sản phẩm
     * 
     * @param productId ID sản phẩm
     * @return danh sách hình ảnh
     */
    List<ProductImageDto> getProductImages(Long productId);

    /**
     * Lấy hình ảnh của biến thể cụ thể
     * 
     * @param variantId ID biến thể
     * @return danh sách hình ảnh của biến thể
     */
    List<ProductImageDto> getVariantImages(Integer variantId);

    /**
     * Lấy hình ảnh chính của sản phẩm
     * 
     * @param productId ID sản phẩm
     * @return hình ảnh chính
     */
    ProductImageDto getMainProductImage(Long productId);

    /**
     * Xóa hình ảnh theo ID
     * 
     * @param imageId ID hình ảnh
     * @return true nếu xóa thành công
     */
    boolean deleteProductImage(Integer imageId);

    /**
     * Xóa tất cả hình ảnh của sản phẩm
     * 
     * @param productId ID sản phẩm
     * @return số lượng ảnh đã xóa
     */
    int deleteAllProductImages(Long productId);

    /**
     * Xóa tất cả hình ảnh của biến thể
     * 
     * @param variantId ID biến thể
     * @return số lượng ảnh đã xóa
     */
    int deleteAllVariantImages(Integer variantId);

    /**
     * Cập nhật thứ tự sắp xếp hình ảnh
     * 
     * @param imageId      ID hình ảnh
     * @param newSortOrder thứ tự mới
     * @return hình ảnh đã cập nhật
     */
    ProductImageDto updateImageSortOrder(Integer imageId, Integer newSortOrder);

    /**
     * Cập nhật văn bản thay thế cho hình ảnh
     * 
     * @param imageId  ID hình ảnh
     * @param imageAlt văn bản thay thế mới
     * @return hình ảnh đã cập nhật
     */
    ProductImageDto updateImageAlt(Integer imageId, String imageAlt);

    /**
     * Kiểm tra số lượng hình ảnh của sản phẩm
     * 
     * @param productId ID sản phẩm
     * @return số lượng hình ảnh
     */
    long countProductImages(Long productId);

    /**
     * Kiểm tra số lượng hình ảnh của biến thể
     * 
     * @param variantId ID biến thể
     * @return số lượng hình ảnh
     */
    long countVariantImages(Integer variantId);
}
