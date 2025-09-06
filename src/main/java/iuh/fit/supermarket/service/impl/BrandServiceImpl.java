package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.brand.BrandCreateRequest;
import iuh.fit.supermarket.dto.brand.BrandResponse;
import iuh.fit.supermarket.dto.brand.BrandUpdateRequest;
import iuh.fit.supermarket.entity.Brand;
import iuh.fit.supermarket.repository.BrandRepository;
import iuh.fit.supermarket.service.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của BrandService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    /**
     * Tạo mới thương hiệu
     */
    @Override
    public BrandResponse createBrand(BrandCreateRequest request) {
        log.info("Tạo mới thương hiệu với tên: {}", request.getName());
        
        try {
            // Kiểm tra tên thương hiệu đã tồn tại
            if (brandRepository.existsByName(request.getName())) {
                throw new IllegalArgumentException("Tên thương hiệu đã tồn tại: " + request.getName());
            }

            // Tạo mã thương hiệu tự động nếu không được cung cấp
            String brandCode = request.getBrandCode();
            if (!StringUtils.hasText(brandCode)) {
                brandCode = generateBrandCode();
            } else {
                // Kiểm tra mã thương hiệu đã tồn tại
                if (brandRepository.existsByBrandCode(brandCode)) {
                    throw new IllegalArgumentException("Mã thương hiệu đã tồn tại: " + brandCode);
                }
            }

            // Tạo entity Brand
            Brand brand = new Brand();
            brand.setName(request.getName());
            brand.setBrandCode(brandCode);
            brand.setLogoUrl(request.getLogoUrl());
            brand.setDescription(request.getDescription());
            brand.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

            // Lưu vào database
            Brand savedBrand = brandRepository.save(brand);
            
            log.info("Tạo thương hiệu thành công với ID: {}", savedBrand.getBrandId());
            return convertToResponse(savedBrand);
            
        } catch (IllegalArgumentException e) {
            log.error("Lỗi validation khi tạo thương hiệu: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Lỗi không mong muốn khi tạo thương hiệu: ", e);
            throw new RuntimeException("Không thể tạo thương hiệu: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật thông tin thương hiệu
     */
    @Override
    public BrandResponse updateBrand(Integer brandId, BrandUpdateRequest request) {
        log.info("Cập nhật thương hiệu ID: {} với tên: {}", brandId, request.getName());
        
        try {
            // Tìm thương hiệu cần cập nhật
            Brand existingBrand = findBrandEntityById(brandId);

            // Kiểm tra tên thương hiệu đã tồn tại (loại trừ thương hiệu hiện tại)
            if (brandRepository.existsByNameAndBrandIdNot(request.getName(), brandId)) {
                throw new IllegalArgumentException("Tên thương hiệu đã tồn tại: " + request.getName());
            }

            // Kiểm tra mã thương hiệu nếu được cung cấp
            if (StringUtils.hasText(request.getBrandCode()) && 
                brandRepository.existsByBrandCodeAndBrandIdNot(request.getBrandCode(), brandId)) {
                throw new IllegalArgumentException("Mã thương hiệu đã tồn tại: " + request.getBrandCode());
            }

            // Cập nhật thông tin
            existingBrand.setName(request.getName());
            if (StringUtils.hasText(request.getBrandCode())) {
                existingBrand.setBrandCode(request.getBrandCode());
            }
            existingBrand.setLogoUrl(request.getLogoUrl());
            existingBrand.setDescription(request.getDescription());
            if (request.getIsActive() != null) {
                existingBrand.setIsActive(request.getIsActive());
            }

            // Lưu cập nhật
            Brand updatedBrand = brandRepository.save(existingBrand);
            
            log.info("Cập nhật thương hiệu thành công ID: {}", brandId);
            return convertToResponse(updatedBrand);
            
        } catch (IllegalArgumentException e) {
            log.error("Lỗi validation khi cập nhật thương hiệu: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Lỗi không mong muốn khi cập nhật thương hiệu: ", e);
            throw new RuntimeException("Không thể cập nhật thương hiệu: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa thương hiệu (soft delete)
     */
    @Override
    public void deleteBrand(Integer brandId) {
        log.info("Xóa thương hiệu ID: {}", brandId);
        
        try {
            Brand brand = findBrandEntityById(brandId);
            
            // Kiểm tra xem thương hiệu có sản phẩm nào không
            if (brand.getProducts() != null && !brand.getProducts().isEmpty()) {
                throw new IllegalStateException("Không thể xóa thương hiệu vì còn có sản phẩm liên kết");
            }

            // Soft delete bằng cách set isActive = false
            brand.setIsActive(false);
            brandRepository.save(brand);
            
            log.info("Xóa thương hiệu thành công ID: {}", brandId);
            
        } catch (IllegalStateException e) {
            log.error("Lỗi business logic khi xóa thương hiệu: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Lỗi không mong muốn khi xóa thương hiệu: ", e);
            throw new RuntimeException("Không thể xóa thương hiệu: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy thông tin thương hiệu theo ID
     */
    @Override
    @Transactional(readOnly = true)
    public BrandResponse getBrandById(Integer brandId) {
        log.debug("Lấy thông tin thương hiệu ID: {}", brandId);
        
        try {
            Brand brand = findBrandEntityById(brandId);
            return convertToResponse(brand);
        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin thương hiệu: ", e);
            throw new RuntimeException("Không thể lấy thông tin thương hiệu: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy danh sách tất cả thương hiệu đang hoạt động
     */
    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> getAllActiveBrands() {
        log.debug("Lấy danh sách tất cả thương hiệu đang hoạt động");
        
        try {
            List<Brand> brands = brandRepository.findByIsActiveTrue();
            return brands.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách thương hiệu: ", e);
            throw new RuntimeException("Không thể lấy danh sách thương hiệu: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy danh sách thương hiệu với phân trang
     */
    @Override
    @Transactional(readOnly = true)
    public Page<BrandResponse> getAllBrands(Pageable pageable) {
        log.debug("Lấy danh sách thương hiệu với phân trang: {}", pageable);
        
        try {
            Page<Brand> brandPage = brandRepository.findByIsActiveTrue(pageable);
            return brandPage.map(this::convertToResponse);
        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách thương hiệu có phân trang: ", e);
            throw new RuntimeException("Không thể lấy danh sách thương hiệu: " + e.getMessage(), e);
        }
    }

    /**
     * Tìm kiếm thương hiệu theo tên
     */
    @Override
    @Transactional(readOnly = true)
    public List<BrandResponse> searchBrands(String keyword) {
        log.debug("Tìm kiếm thương hiệu với từ khóa: {}", keyword);
        
        try {
            if (!StringUtils.hasText(keyword)) {
                return getAllActiveBrands();
            }
            
            List<Brand> brands = brandRepository.findByNameContaining(keyword.trim());
            return brands.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm thương hiệu: ", e);
            throw new RuntimeException("Không thể tìm kiếm thương hiệu: " + e.getMessage(), e);
        }
    }

    /**
     * Tìm kiếm thương hiệu theo tên với phân trang
     */
    @Override
    @Transactional(readOnly = true)
    public Page<BrandResponse> searchBrands(String keyword, Pageable pageable) {
        log.debug("Tìm kiếm thương hiệu với từ khóa: {} và phân trang: {}", keyword, pageable);
        
        try {
            if (!StringUtils.hasText(keyword)) {
                return getAllBrands(pageable);
            }
            
            Page<Brand> brandPage = brandRepository.findByNameContaining(keyword.trim(), pageable);
            return brandPage.map(this::convertToResponse);
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm thương hiệu có phân trang: ", e);
            throw new RuntimeException("Không thể tìm kiếm thương hiệu: " + e.getMessage(), e);
        }
    }

    /**
     * Kiểm tra thương hiệu có tồn tại không
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Integer brandId) {
        try {
            return brandRepository.existsById(brandId);
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra tồn tại thương hiệu: ", e);
            return false;
        }
    }

    /**
     * Lấy entity Brand theo ID (dùng nội bộ)
     */
    @Override
    @Transactional(readOnly = true)
    public Brand findBrandEntityById(Integer brandId) {
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thương hiệu với ID: " + brandId));
    }

    /**
     * Kích hoạt/vô hiệu hóa thương hiệu
     */
    @Override
    public BrandResponse toggleBrandStatus(Integer brandId, Boolean isActive) {
        log.info("Thay đổi trạng thái thương hiệu ID: {} thành: {}", brandId, isActive);
        
        try {
            Brand brand = findBrandEntityById(brandId);
            brand.setIsActive(isActive);
            Brand updatedBrand = brandRepository.save(brand);
            
            log.info("Thay đổi trạng thái thương hiệu thành công ID: {}", brandId);
            return convertToResponse(updatedBrand);
            
        } catch (Exception e) {
            log.error("Lỗi khi thay đổi trạng thái thương hiệu: ", e);
            throw new RuntimeException("Không thể thay đổi trạng thái thương hiệu: " + e.getMessage(), e);
        }
    }

    /**
     * Tạo mã thương hiệu tự động
     */
    private String generateBrandCode() {
        try {
            List<String> maxCodes = brandRepository.findMaxBrandCode();
            
            if (maxCodes.isEmpty()) {
                return "BR0001";
            }
            
            String maxCode = maxCodes.get(0);
            int nextNumber = Integer.parseInt(maxCode.substring(2)) + 1;
            return String.format("BR%04d", nextNumber);
            
        } catch (Exception e) {
            log.error("Lỗi khi tạo mã thương hiệu tự động: ", e);
            // Fallback: tạo mã dựa trên timestamp
            return "BR" + String.format("%04d", (int)(System.currentTimeMillis() % 10000));
        }
    }

    /**
     * Chuyển đổi Brand entity thành BrandResponse DTO
     */
    private BrandResponse convertToResponse(Brand brand) {
        BrandResponse response = new BrandResponse();
        response.setBrandId(brand.getBrandId());
        response.setName(brand.getName());
        response.setBrandCode(brand.getBrandCode());
        response.setLogoUrl(brand.getLogoUrl());
        response.setDescription(brand.getDescription());
        response.setIsActive(brand.getIsActive());
        response.setCreatedAt(brand.getCreatedAt());
        response.setUpdatedAt(brand.getUpdatedAt());
        
        // Đếm số lượng sản phẩm
        if (brand.getProducts() != null) {
            response.setProductCount(brand.getProducts().size());
        } else {
            response.setProductCount(0);
        }
        
        return response;
    }
}