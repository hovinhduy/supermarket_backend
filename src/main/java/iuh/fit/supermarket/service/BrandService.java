package iuh.fit.supermarket.service;

import iuh.fit.supermarket.dto.brand.BrandCreateRequest;
import iuh.fit.supermarket.dto.brand.BrandResponse;
import iuh.fit.supermarket.dto.brand.BrandUpdateRequest;
import iuh.fit.supermarket.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface cho quản lý thương hiệu
 */
public interface BrandService {

    /**
     * Tạo mới thương hiệu
     * @param request thông tin thương hiệu cần tạo
     * @return BrandResponse thông tin thương hiệu đã tạo
     */
    BrandResponse createBrand(BrandCreateRequest request);

    /**
     * Cập nhật thông tin thương hiệu
     * @param brandId ID thương hiệu cần cập nhật
     * @param request thông tin cập nhật
     * @return BrandResponse thông tin thương hiệu đã cập nhật
     */
    BrandResponse updateBrand(Integer brandId, BrandUpdateRequest request);

    /**
     * Xóa thương hiệu (soft delete)
     * @param brandId ID thương hiệu cần xóa
     */
    void deleteBrand(Integer brandId);

    /**
     * Lấy thông tin thương hiệu theo ID
     * @param brandId ID thương hiệu
     * @return BrandResponse thông tin thương hiệu
     */
    BrandResponse getBrandById(Integer brandId);

    /**
     * Lấy danh sách tất cả thương hiệu đang hoạt động
     * @return List<BrandResponse> danh sách thương hiệu
     */
    List<BrandResponse> getAllActiveBrands();

    /**
     * Lấy danh sách thương hiệu với phân trang
     * @param pageable thông tin phân trang
     * @return Page<BrandResponse> danh sách thương hiệu có phân trang
     */
    Page<BrandResponse> getAllBrands(Pageable pageable);

    /**
     * Tìm kiếm thương hiệu theo tên
     * @param keyword từ khóa tìm kiếm
     * @return List<BrandResponse> danh sách thương hiệu tìm được
     */
    List<BrandResponse> searchBrands(String keyword);

    /**
     * Tìm kiếm thương hiệu theo tên với phân trang
     * @param keyword từ khóa tìm kiếm
     * @param pageable thông tin phân trang
     * @return Page<BrandResponse> danh sách thương hiệu tìm được có phân trang
     */
    Page<BrandResponse> searchBrands(String keyword, Pageable pageable);

    /**
     * Kiểm tra thương hiệu có tồn tại không
     * @param brandId ID thương hiệu
     * @return true nếu tồn tại
     */
    boolean existsById(Integer brandId);

    /**
     * Lấy entity Brand theo ID (dùng nội bộ)
     * @param brandId ID thương hiệu
     * @return Brand entity
     */
    Brand findBrandEntityById(Integer brandId);

    /**
     * Kích hoạt/vô hiệu hóa thương hiệu
     * @param brandId ID thương hiệu
     * @param isActive trạng thái mới
     * @return BrandResponse thông tin thương hiệu đã cập nhật
     */
    BrandResponse toggleBrandStatus(Integer brandId, Boolean isActive);
}