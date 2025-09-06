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
     * Lấy danh sách sản phẩm với phân trang
     * 
     * @param pageable thông tin phân trang
     * @return danh sách sản phẩm
     */
    Page<ProductResponse> getProducts(Pageable pageable);

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
     * Tạo mã sản phẩm tự động
     * 
     * @return mã sản phẩm mới
     */
    String generateProductCode();
}
