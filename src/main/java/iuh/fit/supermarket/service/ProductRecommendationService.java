package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.product.ProductResponse;

import java.util.List;

/**
 * Service interface cho tính năng gợi ý sản phẩm cho AI
 */
public interface ProductRecommendationService {

    /**
     * Tìm kiếm sản phẩm theo từ khóa (cho AI sử dụng)
     * 
     * @param keyword từ khóa tìm kiếm
     * @return danh sách sản phẩm phù hợp
     */
    List<ProductResponse> searchProducts(String keyword);

    /**
     * Lấy thông tin chi tiết sản phẩm (cho AI sử dụng)
     * 
     * @param productId ID sản phẩm
     * @return thông tin chi tiết sản phẩm
     */
    ProductResponse getProductDetails(Long productId);

    /**
     * Lấy danh sách sản phẩm theo danh mục (cho AI sử dụng)
     * 
     * @param categoryName tên danh mục
     * @return danh sách sản phẩm trong danh mục
     */
    List<ProductResponse> getProductsByCategory(String categoryName);

    /**
     * Lấy danh sách sản phẩm theo thương hiệu (cho AI sử dụng)
     * 
     * @param brandName tên thương hiệu
     * @return danh sách sản phẩm của thương hiệu
     */
    List<ProductResponse> getProductsByBrand(String brandName);
}
