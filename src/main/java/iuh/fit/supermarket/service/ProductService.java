package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.product.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface cho quản lý sản phẩm
 */
public interface ProductService {

    /**
     * Tạo sản phẩm mới
     * 
     * @param request thông tin sản phẩm cần tạo
     * @return thông tin sản phẩm đã tạo
     */
    ProductResponse createProduct(ProductCreateRequest request);

    /**
     * Lấy thông tin sản phẩm theo ID
     * 
     * @param id ID sản phẩm
     * @return thông tin sản phẩm
     */
    ProductResponse getProductById(Long id);

    /**
     * Cập nhật thông tin sản phẩm
     * 
     * @param id      ID sản phẩm
     * @param request thông tin cập nhật
     * @return thông tin sản phẩm đã cập nhật
     */
    ProductResponse updateProduct(Long id, ProductUpdateRequest request);

    /**
     * Xóa sản phẩm (soft delete)
     * 
     * @param id ID sản phẩm
     */
    void deleteProduct(Long id);

    /**
     * Xóa nhiều sản phẩm cùng lúc (soft delete)
     * 
     * @param ids Danh sách ID sản phẩm cần xóa
     */
    void deleteProducts(List<Long> ids);

    /**
     * Lấy danh sách sản phẩm với phân trang
     * 
     * @param pageable thông tin phân trang
     * @return danh sách sản phẩm
     */
    Page<ProductResponse> getProducts(Pageable pageable);

    /**
     * Lấy danh sách sản phẩm với filtering, searching và sorting nâng cao
     * 
     * @param request thông tin phân trang, filtering và sorting
     * @return danh sách sản phẩm
     */
    Page<ProductResponse> getProductsAdvanced(ProductPageableRequest request);

    /**
     * Tìm kiếm sản phẩm theo từ khóa
     * 
     * @param keyword từ khóa tìm kiếm
     * @return danh sách sản phẩm tìm được
     */
    List<ProductResponse> searchProducts(String keyword);

    /**
     * Lấy danh sách sản phẩm theo danh mục
     * 
     * @param categoryId ID danh mục
     * @return danh sách sản phẩm
     */
    List<ProductResponse> getProductsByCategory(Long categoryId);

    /**
     * Tạo biến thể sản phẩm
     * 
     * @param productId ID sản phẩm gốc
     * @param request   thông tin biến thể
     * @return thông tin biến thể đã tạo
     */
    ProductResponse createProductVariant(Long productId, ProductVariantCreateRequest request);

    /**
     * Lấy danh sách sản phẩm có tồn kho thấp
     * 
     * @return danh sách sản phẩm
     */
    List<ProductResponse> getLowStockProducts();

    /**
     * Tạo sản phẩm mới với nhiều biến thể cùng lúc
     * 
     * @param request thông tin sản phẩm và các biến thể
     * @return thông tin sản phẩm đã tạo
     */
    ProductResponse createProductWithVariants(ProductCreateWithVariantsRequest request);

    /**
     * Tạo mã sản phẩm tự động
     * 
     * @return mã sản phẩm mới
     */
    String generateProductCode();

    /**
     * Cập nhật thông tin biến thể sản phẩm
     * 
     * @param variantId ID biến thể
     * @param request   thông tin cập nhật biến thế
     * @return thông tin biến thể đã cập nhật
     */
    ProductVariantDto updateProductVariant(Long variantId, ProductVariantUpdateRequest request);

    /**
     * Lấy thông tin biến thể theo ID
     *
     * @param variantId ID biến thể
     * @return thông tin biến thể
     */
    ProductVariantDto getProductVariantById(Long variantId);

    /**
     * Lấy danh sách biến thể theo ID sản phẩm
     *
     * @param productId ID sản phẩm
     * @return danh sách biến thể của sản phẩm
     */
    List<ProductVariantDto> getProductVariantsByProductId(Long productId);

    /**
     * Xóa nhiều biến thể cùng lúc (soft delete)
     * 
     * @param variantIds Danh sách ID biến thể cần xóa
     */
    void deleteProductVariants(List<Long> variantIds);

    /**
     * Xóa một biến thể (soft delete)
     * 
     * @param variantId ID biến thể cần xóa
     */
    void deleteProductVariant(Long variantId);
}
