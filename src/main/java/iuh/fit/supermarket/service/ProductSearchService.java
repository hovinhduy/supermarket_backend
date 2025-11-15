package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.chat.structured.ProductInfo;

import java.util.List;

/**
 * Service interface cho tìm kiếm sản phẩm (dùng cho AI Chat)
 */
public interface ProductSearchService {

    /**
     * Tìm kiếm sản phẩm theo tên hoặc từ khóa
     * Method này được gọi bởi AI thông qua function calling
     *
     * @param searchTerm từ khóa tìm kiếm (tên sản phẩm, mã sản phẩm)
     * @param limit số lượng kết quả tối đa (mặc định 10)
     * @return danh sách thông tin sản phẩm dạng ProductInfo
     */
    List<ProductInfo> searchProducts(String searchTerm, Integer limit);

    /**
     * Lấy thông tin chi tiết sản phẩm theo ID
     *
     * @param productId ID sản phẩm
     * @return thông tin sản phẩm hoặc null nếu không tìm thấy
     */
    ProductInfo getProductById(Long productId);
}
