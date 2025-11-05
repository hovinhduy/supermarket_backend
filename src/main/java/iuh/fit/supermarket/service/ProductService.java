package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.product.ProductCreateRequest;
import iuh.fit.supermarket.dto.product.ProductListResponse;
import iuh.fit.supermarket.dto.product.ProductResponse;
import iuh.fit.supermarket.dto.product.ProductUpdateRequest;
import iuh.fit.supermarket.dto.product.ProductUnitRequest;
import iuh.fit.supermarket.dto.product.ProductUnitUpdateRequest;
import iuh.fit.supermarket.dto.product.ProductUnitResponse;
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
     * Xóa nhiều sản phẩm (soft delete)
     * 
     * @param ids danh sách ID sản phẩm cần xóa
     */
    void deleteMultipleProducts(List<Long> ids);

    /**
     * Lấy danh sách sản phẩm với phân trang và tìm kiếm/lọc
     *
     * @param searchTerm    từ khóa tìm kiếm (tìm theo tên sản phẩm hoặc mã sản phẩm)
     * @param categoryId    ID danh mục để lọc (tùy chọn)
     * @param brandId       ID thương hiệu để lọc (tùy chọn)
     * @param isActive      trạng thái hoạt động để lọc (tùy chọn)
     * @param isRewardPoint có tích điểm thưởng để lọc (tùy chọn)
     * @param pageable      thông tin phân trang
     * @return danh sách sản phẩm với phân trang
     */
    ProductListResponse getProducts(String searchTerm,
            Integer categoryId,
            Integer brandId,
            Boolean isActive,
            Boolean isRewardPoint,
            Pageable pageable);

    /**
     * Lấy tất cả sản phẩm đang hoạt động (không phân trang)
     * 
     * @return danh sách sản phẩm đang hoạt động
     */
    List<ProductResponse> getAllActiveProducts();

    /**
     * Lấy danh sách sản phẩm theo danh mục
     * 
     * @param categoryId ID danh mục
     * @param pageable   thông tin phân trang
     * @return danh sách sản phẩm
     */
    ProductListResponse getProductsByCategory(Integer categoryId, Pageable pageable);

    /**
     * Lấy danh sách sản phẩm theo thương hiệu
     * 
     * @param brandId  ID thương hiệu
     * @param pageable thông tin phân trang
     * @return danh sách sản phẩm
     */
    ProductListResponse getProductsByBrand(Integer brandId, Pageable pageable);

    /**
     * Tìm kiếm sản phẩm theo tên
     * 
     * @param keyword  từ khóa tìm kiếm
     * @param pageable thông tin phân trang
     * @return danh sách sản phẩm tìm được
     */
    ProductListResponse searchProducts(String keyword, Pageable pageable);

    /**
     * Kiểm tra sản phẩm có tồn tại không
     * 
     * @param id ID sản phẩm
     * @return true nếu tồn tại, false nếu không
     */
    boolean existsById(Long id);

    /**
     * Kiểm tra tên sản phẩm có bị trùng không
     * 
     * @param name tên sản phẩm
     * @return true nếu trùng, false nếu không
     */
    boolean existsByName(String name);

    /**
     * Kiểm tra tên sản phẩm có bị trùng không (loại trừ ID hiện tại)
     * 
     * @param name      tên sản phẩm
     * @param excludeId ID sản phẩm được loại trừ
     * @return true nếu trùng, false nếu không
     */
    boolean existsByNameAndIdNot(String name, Long excludeId);

    /**
     * Kiểm tra sản phẩm có đơn vị cơ bản không
     * 
     * @param productId ID sản phẩm
     * @return true nếu có đơn vị cơ bản, false nếu không
     */
    boolean hasBaseUnit(Long productId);

    // ==================== QUẢN LÝ ĐƠN VỊ SẢN PHẨM ====================

    /**
     * Thêm đơn vị mới vào sản phẩm
     * 
     * @param productId ID sản phẩm
     * @param request   thông tin đơn vị cần thêm
     * @return thông tin đơn vị sản phẩm đã tạo
     */
    ProductUnitResponse addProductUnit(Long productId, ProductUnitRequest request);

    /**
     * Cập nhật thông tin đơn vị sản phẩm
     * 
     * @param productId ID sản phẩm
     * @param unitId    ID đơn vị sản phẩm
     * @param request   thông tin cập nhật
     * @return thông tin đơn vị sản phẩm đã cập nhật
     */
    ProductUnitResponse updateProductUnit(Long productId, Long unitId, ProductUnitUpdateRequest request);

    /**
     * Xóa đơn vị khỏi sản phẩm (soft delete)
     * 
     * @param productId ID sản phẩm
     * @param unitId    ID đơn vị sản phẩm
     */
    void deleteProductUnit(Long productId, Long unitId);

    /**
     * Lấy danh sách đơn vị của sản phẩm
     * 
     * @param productId ID sản phẩm
     * @return danh sách đơn vị sản phẩm
     */
    List<ProductUnitResponse> getProductUnits(Long productId);

    /**
     * Lấy thông tin đơn vị sản phẩm theo ID
     * 
     * @param productId ID sản phẩm
     * @param unitId    ID đơn vị sản phẩm
     * @return thông tin đơn vị sản phẩm
     */
    ProductUnitResponse getProductUnit(Long productId, Long unitId);

    /**
     * Lấy đơn vị cơ bản của sản phẩm
     * 
     * @param productId ID sản phẩm
     * @return thông tin đơn vị cơ bản
     */
    ProductUnitResponse getBaseProductUnit(Long productId);

    /**
     * Tìm kiếm ProductUnit theo tên sản phẩm, mã code hoặc barcode
     * 
     * @param searchTerm từ khóa tìm kiếm
     * @return danh sách ProductUnit tìm được
     */
    List<ProductUnitResponse> searchProductUnits(String searchTerm);

    /**
     * Lấy thông tin chi tiết đầy đủ của ProductUnit
     * Bao gồm tên sản phẩm, tên đơn vị, số lượng tồn kho và giá hiện tại
     * 
     * @param productUnitId ID đơn vị sản phẩm
     * @return thông tin chi tiết đầy đủ của ProductUnit
     */
    iuh.fit.supermarket.dto.product.ProductUnitDetailResponse getProductUnitDetails(Long productUnitId);
}
