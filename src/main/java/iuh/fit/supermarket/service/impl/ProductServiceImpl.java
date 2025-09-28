package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.product.*;
import iuh.fit.supermarket.dto.unit.UnitDto;
import iuh.fit.supermarket.entity.Brand;
import iuh.fit.supermarket.entity.Category;
import iuh.fit.supermarket.entity.Product;
import iuh.fit.supermarket.entity.ProductUnit;
import iuh.fit.supermarket.entity.Unit;
import iuh.fit.supermarket.exception.DuplicateProductException;
import iuh.fit.supermarket.exception.ProductException;
import iuh.fit.supermarket.exception.ProductNotFoundException;
import iuh.fit.supermarket.repository.BrandRepository;
import iuh.fit.supermarket.repository.CategoryRepository;
import iuh.fit.supermarket.repository.ProductRepository;
import iuh.fit.supermarket.repository.ProductUnitRepository;
import iuh.fit.supermarket.repository.UnitRepository;
import iuh.fit.supermarket.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation của ProductService
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final UnitRepository unitRepository;
    private final ProductUnitRepository productUnitRepository;

    /**
     * Tạo sản phẩm mới
     */
    @Override
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("Bắt đầu tạo sản phẩm mới: {}", request.getName());

        // Kiểm tra trùng lặp tên sản phẩm
        if (existsByName(request.getName())) {
            throw DuplicateProductException.forProductName(request.getName());
        }

        // Validation danh sách units
        validateUnits(request.getUnits());

        // Kiểm tra dữ liệu đầu vào
        if (request.getCategoryId() == null) {
            throw new ProductException("ID danh mục không được để trống");
        }

        // Kiểm tra danh mục tồn tại và hoạt động
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ProductException(
                        "Không tìm thấy danh mục với ID: " + request.getCategoryId()));

        if (!category.getIsActive()) {
            throw new ProductException("Danh mục đã bị vô hiệu hóa");
        }

        // Kiểm tra thương hiệu nếu có cung cấp
        Brand brand = null;
        if (request.getBrandId() != null) {
            brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ProductException(
                            "Không tìm thấy thương hiệu với ID: " + request.getBrandId()));

            if (!brand.getIsActive()) {
                throw new ProductException("Thương hiệu đã bị vô hiệu hóa");
            }
        }

        // Tạo entity Product
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCategory(category);
        product.setBrand(brand); // Có thể null nếu không cung cấp brandId
        product.setIsRewardPoint(request.getIsRewardPoint() != null ? request.getIsRewardPoint() : false);
        product.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        product.setIsDeleted(false);

        // Lưu sản phẩm
        product = productRepository.save(product);
        log.info("Đã tạo sản phẩm với ID: {}", product.getId());

        // Tạo các đơn vị cho sản phẩm
        createProductUnits(product, request.getUnits());
        log.info("Đã tạo {} đơn vị cho sản phẩm ID: {}", request.getUnits().size(), product.getId());

        return mapToProductResponse(product);
    }

    /**
     * Lấy thông tin sản phẩm theo ID
     */
    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        log.debug("Lấy thông tin sản phẩm với ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (product.getIsDeleted()) {
            throw new ProductNotFoundException(id);
        }

        return mapToProductResponse(product);
    }

    /**
     * Cập nhật thông tin sản phẩm
     */
    @Override
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        log.info("Bắt đầu cập nhật sản phẩm với ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (product.getIsDeleted()) {
            throw new ProductNotFoundException(id);
        }

        // Kiểm tra trùng lặp tên sản phẩm (nếu có thay đổi tên)
        if (request.getName() != null && !request.getName().equals(product.getName())) {
            if (existsByNameAndIdNot(request.getName(), id)) {
                throw DuplicateProductException.forProductName(request.getName());
            }
            product.setName(request.getName());
        }

        // Cập nhật mô tả
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        // Cập nhật danh mục
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ProductException(
                            "Không tìm thấy danh mục với ID: " + request.getCategoryId()));

            if (!category.getIsActive()) {
                throw new ProductException("Danh mục đã bị vô hiệu hóa");
            }
            product.setCategory(category);
        }

        // Cập nhật thương hiệu (nếu có)
        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(() -> new ProductException(
                            "Không tìm thấy thương hiệu với ID: " + request.getBrandId()));

            if (!brand.getIsActive()) {
                throw new ProductException("Thương hiệu đã bị vô hiệu hóa");
            }
            product.setBrand(brand);
        }

        // Cập nhật các trường khác
        if (request.getIsRewardPoint() != null) {
            product.setIsRewardPoint(request.getIsRewardPoint());
        }

        if (request.getIsActive() != null) {
            product.setIsActive(request.getIsActive());
        }

        // Lưu thay đổi
        product = productRepository.save(product);
        log.info("Đã cập nhật sản phẩm với ID: {}", product.getId());

        return mapToProductResponse(product);
    }

    /**
     * Xóa sản phẩm (soft delete)
     */
    @Override
    public void deleteProduct(Long id) {
        log.info("Bắt đầu xóa sản phẩm với ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (product.getIsDeleted()) {
            throw new ProductNotFoundException(id);
        }

        // Thực hiện soft delete cho sản phẩm
        product.setIsDeleted(true);
        product.setIsActive(false);
        productRepository.save(product);

        // Soft delete tất cả đơn vị sản phẩm liên quan
        List<ProductUnit> productUnits = productUnitRepository.findByProductId(id);
        for (ProductUnit productUnit : productUnits) {
            productUnit.setIsDeleted(true);
            productUnit.setIsActive(false);
        }
        productUnitRepository.saveAll(productUnits);

        log.info("Đã xóa sản phẩm và {} đơn vị sản phẩm với ID: {}", productUnits.size(), id);
    }

    /**
     * Xóa nhiều sản phẩm (soft delete)
     */
    @Override
    public void deleteMultipleProducts(List<Long> ids) {
        log.info("Bắt đầu xóa {} sản phẩm", ids.size());

        if (ids == null || ids.isEmpty()) {
            throw new ProductException("Danh sách ID sản phẩm không được rỗng");
        }

        List<Product> products = productRepository.findAllById(ids);

        if (products.size() != ids.size()) {
            throw new ProductException("Một số sản phẩm không tồn tại hoặc đã bị xóa");
        }

        // Thực hiện soft delete cho tất cả sản phẩm và đơn vị sản phẩm liên quan
        int totalProductUnits = 0;
        for (Product product : products) {
            if (!product.getIsDeleted()) {
                product.setIsDeleted(true);
                product.setIsActive(false);

                // Soft delete tất cả đơn vị sản phẩm liên quan
                List<ProductUnit> productUnits = productUnitRepository.findByProductId(product.getId());
                for (ProductUnit productUnit : productUnits) {
                    productUnit.setIsDeleted(true);
                    productUnit.setIsActive(false);
                }
                productUnitRepository.saveAll(productUnits);
                totalProductUnits += productUnits.size();
            }
        }

        productRepository.saveAll(products);
        log.info("Đã xóa {} sản phẩm và {} đơn vị sản phẩm", products.size(), totalProductUnits);
    }

    /**
     * Lấy danh sách sản phẩm với phân trang và tìm kiếm/lọc
     */
    @Override
    @Transactional(readOnly = true)
    public ProductListResponse getProducts(String searchTerm,
            Integer categoryId,
            Integer brandId,
            Boolean isActive,
            Boolean isRewardPoint,
            Pageable pageable) {
        log.debug(
                "Lấy danh sách sản phẩm với filter: searchTerm={}, categoryId={}, brandId={}, isActive={}, isRewardPoint={}",
                searchTerm, categoryId, brandId, isActive, isRewardPoint);

        // Tạm thời sử dụng method đơn giản, sau này có thể mở rộng với Specification
        Page<Product> productPage;

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            productPage = productRepository.findProductsAdvanced(searchTerm.trim(), isActive, pageable);
        } else {
            productPage = productRepository.findProductsAdvanced("", isActive, pageable);
        }

        return mapToProductListResponse(productPage);
    }

    /**
     * Lấy tất cả sản phẩm đang hoạt động
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllActiveProducts() {
        log.debug("Lấy tất cả sản phẩm đang hoạt động");

        List<Product> products = productRepository.findByIsActiveAndIsDeleted(true, false);
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách sản phẩm theo danh mục
     */
    @Override
    @Transactional(readOnly = true)
    public ProductListResponse getProductsByCategory(Integer categoryId, Pageable pageable) {
        log.debug("Lấy danh sách sản phẩm theo danh mục ID: {}", categoryId);

        // Kiểm tra danh mục tồn tại
        if (!categoryRepository.existsById(categoryId)) {
            throw new ProductException("Không tìm thấy danh mục với ID: " + categoryId);
        }

        List<Product> products = productRepository.findByCategoryIdAndIsDeleted(categoryId.longValue(), false);

        // Tạm thời convert thành Page manually, sau này có thể cải thiện
        return createProductListResponse(products, pageable);
    }

    /**
     * Lấy danh sách sản phẩm theo thương hiệu
     */
    @Override
    @Transactional(readOnly = true)
    public ProductListResponse getProductsByBrand(Integer brandId, Pageable pageable) {
        log.debug("Lấy danh sách sản phẩm theo thương hiệu ID: {}", brandId);

        // Kiểm tra thương hiệu tồn tại
        if (!brandRepository.existsById(brandId)) {
            throw new ProductException("Không tìm thấy thương hiệu với ID: " + brandId);
        }

        // Tạm thời sử dụng phương pháp đơn giản
        List<Product> allProducts = productRepository.findByIsActiveAndIsDeleted(true, false);
        List<Product> brandProducts = allProducts.stream()
                .filter(product -> product.getBrand().getBrandId().equals(brandId))
                .collect(Collectors.toList());

        return createProductListResponse(brandProducts, pageable);
    }

    /**
     * Tìm kiếm sản phẩm theo tên
     */
    @Override
    @Transactional(readOnly = true)
    public ProductListResponse searchProducts(String keyword, Pageable pageable) {
        log.debug("Tìm kiếm sản phẩm với từ khóa: {}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new ProductException("Từ khóa tìm kiếm không được rỗng");
        }

        List<Product> products = productRepository.findByNameContaining(keyword.trim());
        return createProductListResponse(products, pageable);
    }

    /**
     * Kiểm tra sản phẩm có tồn tại không
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return productRepository.existsById(id);
    }

    /**
     * Kiểm tra tên sản phẩm có bị trùng không
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return productRepository.findByNameContaining(name).stream()
                .anyMatch(product -> product.getName().equalsIgnoreCase(name) && !product.getIsDeleted());
    }

    /**
     * Kiểm tra tên sản phẩm có bị trùng không (loại trừ ID hiện tại)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean existsByNameAndIdNot(String name, Long excludeId) {
        return productRepository.findByNameContaining(name).stream()
                .anyMatch(product -> product.getName().equalsIgnoreCase(name)
                        && !product.getId().equals(excludeId)
                        && !product.getIsDeleted());
    }

    /**
     * Chuyển đổi Product entity sang ProductResponse DTO
     */
    private ProductResponse mapToProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setIsActive(product.getIsActive());
        response.setIsDeleted(product.getIsDeleted());
        response.setIsRewardPoint(product.getIsRewardPoint());
        response.setCreatedDate(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        // Map thông tin thương hiệu
        if (product.getBrand() != null) {
            ProductResponse.BrandInfo brandInfo = new ProductResponse.BrandInfo();
            brandInfo.setBrandId(product.getBrand().getBrandId());
            brandInfo.setName(product.getBrand().getName());
            brandInfo.setBrandCode(product.getBrand().getBrandCode());
            brandInfo.setLogoUrl(product.getBrand().getLogoUrl());
            response.setBrand(brandInfo);
        }

        // Map thông tin danh mục
        if (product.getCategory() != null) {
            ProductResponse.CategoryInfo categoryInfo = new ProductResponse.CategoryInfo();
            categoryInfo.setCategoryId(product.getCategory().getCategoryId());
            categoryInfo.setName(product.getCategory().getName());
            categoryInfo.setDescription(product.getCategory().getDescription());
            response.setCategory(categoryInfo);
        }

        // Set count thông tin
        response.setUnitCount(product.getProductUnits() != null ? product.getProductUnits().size() : 0);
        response.setImageCount(product.getImages() != null ? product.getImages().size() : 0);

        // Map thông tin các đơn vị sản phẩm
        List<ProductUnit> productUnits = productUnitRepository.findByProductId(product.getId());
        List<ProductResponse.ProductUnitInfo> productUnitInfos = productUnits.stream()
                .filter(pu -> !pu.getIsDeleted()) // Chỉ lấy các unit chưa bị xóa
                .map(this::mapToProductUnitInfo)
                .collect(Collectors.toList());
        response.setProductUnits(productUnitInfos);

        return response;
    }

    /**
     * Chuyển đổi Page<Product> sang ProductListResponse
     */
    private ProductListResponse mapToProductListResponse(Page<Product> productPage) {
        List<ProductListResponse.ProductSummary> productSummaries = productPage.getContent().stream()
                .map(this::mapToProductSummary)
                .collect(Collectors.toList());

        ProductListResponse.PageInfo pageInfo = new ProductListResponse.PageInfo();
        pageInfo.setCurrentPage(productPage.getNumber());
        pageInfo.setPageSize(productPage.getSize());
        pageInfo.setTotalElements(productPage.getTotalElements());
        pageInfo.setTotalPages(productPage.getTotalPages());
        pageInfo.setIsFirst(productPage.isFirst());
        pageInfo.setIsLast(productPage.isLast());
        pageInfo.setHasPrevious(productPage.hasPrevious());
        pageInfo.setHasNext(productPage.hasNext());

        return new ProductListResponse(productSummaries, pageInfo);
    }

    /**
     * Chuyển đổi List<Product> sang ProductListResponse (tạm thời)
     */
    private ProductListResponse createProductListResponse(List<Product> products, Pageable pageable) {
        List<ProductListResponse.ProductSummary> productSummaries = products.stream()
                .map(this::mapToProductSummary)
                .collect(Collectors.toList());

        // Tạo PageInfo giả lập
        ProductListResponse.PageInfo pageInfo = new ProductListResponse.PageInfo();
        pageInfo.setCurrentPage(0);
        pageInfo.setPageSize(products.size());
        pageInfo.setTotalElements((long) products.size());
        pageInfo.setTotalPages(1);
        pageInfo.setIsFirst(true);
        pageInfo.setIsLast(true);
        pageInfo.setHasPrevious(false);
        pageInfo.setHasNext(false);

        return new ProductListResponse(productSummaries, pageInfo);
    }

    /**
     * Chuyển đổi Product entity sang ProductSummary DTO
     */
    private ProductListResponse.ProductSummary mapToProductSummary(Product product) {
        ProductListResponse.ProductSummary summary = new ProductListResponse.ProductSummary();
        summary.setId(product.getId());
        summary.setName(product.getName());
        summary.setDescription(product.getDescription());
        summary.setIsActive(product.getIsActive());
        summary.setIsRewardPoint(product.getIsRewardPoint());
        summary.setCreatedDate(product.getCreatedAt());
        summary.setUpdatedAt(product.getUpdatedAt());

        // Set tên thương hiệu và danh mục
        summary.setBrandName(product.getBrand() != null ? product.getBrand().getName() : null);
        summary.setCategoryName(product.getCategory() != null ? product.getCategory().getName() : null);

        // Load và map thông tin units
        List<ProductUnit> productUnits = productUnitRepository.findActiveByProductId(product.getId());
        List<ProductListResponse.ProductUnitSummary> unitSummaries = productUnits.stream()
                .map(this::mapToProductUnitSummary)
                .collect(Collectors.toList());

        summary.setUnits(unitSummaries);
        summary.setUnitCount(unitSummaries.size());
        summary.setImageCount(product.getImages() != null ? product.getImages().size() : 0);
        summary.setMainImageUrl(null); // Tạm thời null, sau này có thể query thật

        return summary;
    }

    /**
     * Kiểm tra sản phẩm có đơn vị cơ bản không
     */
    @Override
    @Transactional(readOnly = true)
    public boolean hasBaseUnit(Long productId) {
        return productUnitRepository.hasBaseUnit(productId);
    }

    /**
     * Validation danh sách units
     */
    private void validateUnits(List<ProductUnitRequest> units) {
        if (units == null || units.isEmpty()) {
            throw new ProductException("Danh sách đơn vị sản phẩm không được để trống");
        }

        // Kiểm tra phải có ít nhất 1 đơn vị cơ bản
        long baseUnitCount = units.stream()
                .filter(ProductUnitRequest::isBaseUnit)
                .count();

        if (baseUnitCount == 0) {
            throw new ProductException("Phải có ít nhất 1 đơn vị cơ bản (isBaseUnit = true)");
        }

        if (baseUnitCount > 1) {
            throw new ProductException("Chỉ được có 1 đơn vị cơ bản");
        }

        // Kiểm tra không có unitName trùng lặp
        long uniqueUnitNames = units.stream()
                .map(ProductUnitRequest::unitName)
                .map(String::toLowerCase)
                .distinct()
                .count();

        if (uniqueUnitNames != units.size()) {
            throw new ProductException("Không được có đơn vị tính trùng lặp");
        }

        // Kiểm tra code không trùng lặp nếu có
        for (ProductUnitRequest unitRequest : units) {
            if (unitRequest.code() != null && !unitRequest.code().trim().isEmpty()) {
                if (productUnitRepository.existsByCode(unitRequest.code().trim())) {
                    throw new ProductException(
                            "Mã đơn vị sản phẩm '" + unitRequest.code() + "' đã tồn tại trong hệ thống");
                }
            }
        }

        // Kiểm tra barcode không trùng lặp nếu có
        for (ProductUnitRequest unitRequest : units) {
            if (unitRequest.barcode() != null && !unitRequest.barcode().trim().isEmpty()) {
                if (productUnitRepository.existsByBarcode(unitRequest.barcode().trim())) {
                    throw new ProductException("Mã vạch '" + unitRequest.barcode() + "' đã tồn tại trong hệ thống");
                }
            }
        }

        // Kiểm tra không có code trùng lặp trong cùng request
        Set<String> codesInRequest = units.stream()
                .map(ProductUnitRequest::code)
                .filter(code -> code != null && !code.trim().isEmpty())
                .collect(Collectors.toSet());

        if (codesInRequest.size() != units.stream()
                .map(ProductUnitRequest::code)
                .filter(code -> code != null && !code.trim().isEmpty())
                .count()) {
            throw new ProductException("Không được có mã đơn vị sản phẩm trùng lặp trong cùng yêu cầu");
        }
    }

    /**
     * Tạo các đơn vị cho sản phẩm
     */
    private void createProductUnits(Product product, List<ProductUnitRequest> unitRequests) {
        for (ProductUnitRequest unitRequest : unitRequests) {
            // Tìm hoặc tạo Unit theo tên
            Unit unit = findOrCreateUnit(unitRequest.unitName());

            ProductUnit productUnit = new ProductUnit();
            productUnit.setProduct(product);
            productUnit.setUnit(unit);

            // Sử dụng code tùy chỉnh nếu có, ngược lại tự động tạo
            String finalCode;
            if (unitRequest.code() != null && !unitRequest.code().trim().isEmpty()) {
                finalCode = unitRequest.code().trim();
                log.debug("Sử dụng mã tùy chỉnh: {} cho sản phẩm ID: {}", finalCode, product.getId());
            } else {
                finalCode = generateProductUnitCode(product.getId(), unit.getId());
                log.debug("Tự động tạo mã: {} cho sản phẩm ID: {}", finalCode, product.getId());
            }
            productUnit.setCode(finalCode);

            productUnit.setConversionValue(unitRequest.conversionValue());
            productUnit.setIsBaseUnit(unitRequest.isBaseUnit());
            productUnit.setBarcode(unitRequest.barcode());
            productUnit.setIsActive(true);
            productUnit.setIsDeleted(false);

            productUnitRepository.save(productUnit);
            log.debug("Đã tạo đơn vị sản phẩm với mã: {} cho sản phẩm ID: {}",
                    productUnit.getCode(), product.getId());
        }
    }

    /**
     * Tạo mã đơn vị sản phẩm duy nhất
     */
    private String generateProductUnitCode(Long productId, Long unitId) {
        // Format: PU{productId}U{unitId}T{timestamp}
        long timestamp = System.currentTimeMillis() % 100000; // Lấy 5 chữ số cuối
        String code = String.format("PU%dU%dT%d", productId, unitId, timestamp);

        // Kiểm tra trùng lặp và tạo lại nếu cần
        int attempts = 0;
        while (productUnitRepository.existsByCode(code) && attempts < 10) {
            timestamp = System.currentTimeMillis() % 100000;
            code = String.format("PU%dU%dT%d", productId, unitId, timestamp);
            attempts++;
        }

        if (attempts >= 10) {
            throw new ProductException("Không thể tạo mã đơn vị sản phẩm duy nhất sau 10 lần thử");
        }

        return code;
    }

    /**
     * Chuyển đổi ProductUnit entity sang ProductUnitInfo DTO
     */
    private ProductResponse.ProductUnitInfo mapToProductUnitInfo(ProductUnit productUnit) {
        ProductResponse.ProductUnitInfo info = new ProductResponse.ProductUnitInfo();
        info.setId(productUnit.getId());
        info.setCode(productUnit.getCode());
        info.setBarcode(productUnit.getBarcode());
        info.setConversionValue(productUnit.getConversionValue());
        info.setIsBaseUnit(productUnit.getIsBaseUnit());
        info.setIsActive(productUnit.getIsActive());

        // Set tên đơn vị tính
        if (productUnit.getUnit() != null) {
            info.setUnitName(productUnit.getUnit().getName());
        }

        return info;
    }

    /**
     * Tìm hoặc tạo Unit theo tên
     */
    private Unit findOrCreateUnit(String unitName) {
        // Tìm Unit theo tên (case-insensitive)
        Optional<Unit> existingUnit = unitRepository.findByNameIgnoreCase(unitName.trim());

        if (existingUnit.isPresent()) {
            Unit unit = existingUnit.get();
            // Kiểm tra Unit có hoạt động không
            if (!unit.getIsActive() || unit.getIsDeleted()) {
                throw new ProductException("Đơn vị tính '" + unitName + "' đã bị vô hiệu hóa hoặc xóa");
            }
            log.debug("Sử dụng đơn vị tính có sẵn: {}", unitName);
            return unit;
        } else {
            // Tạo Unit mới
            Unit newUnit = new Unit();
            newUnit.setName(unitName.trim());
            newUnit.setIsActive(true);
            newUnit.setIsDeleted(false);

            Unit savedUnit = unitRepository.save(newUnit);
            log.info("Đã tạo đơn vị tính mới: {} với ID: {}", unitName, savedUnit.getId());
            return savedUnit;
        }
    }

    // ==================== QUẢN LÝ ĐƠN VỊ SẢN PHẨM ====================

    /**
     * Thêm đơn vị mới vào sản phẩm
     */
    @Override
    @Transactional
    public ProductUnitResponse addProductUnit(Long productId, ProductUnitRequest request) {
        log.info("Bắt đầu thêm đơn vị mới cho sản phẩm ID: {}, unit: {}", productId, request.unitName());

        // Kiểm tra sản phẩm tồn tại
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));

        // Tìm hoặc tạo Unit theo tên
        Unit unit = findOrCreateUnit(request.unitName());

        // Kiểm tra đã tồn tại ProductUnit với cùng product và unit chưa
        Optional<ProductUnit> existingProductUnit = productUnitRepository.findByProductIdAndUnitId(productId,
                unit.getId());
        if (existingProductUnit.isPresent() && !existingProductUnit.get().getIsDeleted()) {
            throw new ProductException("Đơn vị tính '" + request.unitName() + "' đã tồn tại cho sản phẩm này");
        }

        // Kiểm tra nếu là đơn vị cơ bản thì không được có đơn vị cơ bản khác
        if (request.isBaseUnit()) {
            boolean hasBaseUnit = productUnitRepository.hasBaseUnit(productId);
            if (hasBaseUnit) {
                throw new ProductException("Sản phẩm đã có đơn vị cơ bản. Chỉ được phép có một đơn vị cơ bản duy nhất");
            }
        }

        // Kiểm tra mã code nếu có
        if (request.code() != null && !request.code().trim().isEmpty()) {
            if (productUnitRepository.existsByCode(request.code().trim())) {
                throw new ProductException("Mã đơn vị sản phẩm '" + request.code() + "' đã tồn tại");
            }
        }

        // Kiểm tra barcode nếu có
        if (request.barcode() != null && !request.barcode().trim().isEmpty()) {
            if (productUnitRepository.existsByBarcode(request.barcode().trim())) {
                throw new ProductException("Mã vạch '" + request.barcode() + "' đã tồn tại");
            }
        }

        // Tạo ProductUnit mới
        ProductUnit productUnit = new ProductUnit();
        productUnit.setProduct(product);
        productUnit.setUnit(unit);

        // Sử dụng code tùy chỉnh nếu có, ngược lại tự động tạo
        String finalCode;
        if (request.code() != null && !request.code().trim().isEmpty()) {
            finalCode = request.code().trim();
        } else {
            finalCode = generateProductUnitCode(product.getId(), unit.getId());
        }
        productUnit.setCode(finalCode);

        productUnit.setConversionValue(request.conversionValue());
        productUnit.setIsBaseUnit(request.isBaseUnit());
        productUnit.setBarcode(request.barcode());
        productUnit.setIsActive(true);
        productUnit.setIsDeleted(false);

        ProductUnit savedProductUnit = productUnitRepository.save(productUnit);
        log.info("Đã thêm đơn vị sản phẩm với mã: {} cho sản phẩm ID: {}", savedProductUnit.getCode(), productId);

        return mapToProductUnitResponse(savedProductUnit);
    }

    /**
     * Cập nhật thông tin đơn vị sản phẩm
     */
    @Override
    @Transactional
    public ProductUnitResponse updateProductUnit(Long productId, Long unitId, ProductUnitUpdateRequest request) {
        log.info("Bắt đầu cập nhật đơn vị sản phẩm ID: {} của sản phẩm ID: {}", unitId, productId);

        // Kiểm tra có dữ liệu cập nhật không
        if (!request.hasUpdates()) {
            throw new ProductException("Không có dữ liệu để cập nhật");
        }

        // Tìm ProductUnit
        ProductUnit productUnit = productUnitRepository.findByIdAndProductId(unitId, productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Không tìm thấy đơn vị sản phẩm với ID: " + unitId + " thuộc sản phẩm ID: " + productId));

        // Cập nhật tên đơn vị tính nếu có
        if (request.unitName() != null) {
            Unit newUnit = findOrCreateUnit(request.unitName());

            // Kiểm tra không trùng với đơn vị khác của cùng sản phẩm
            Optional<ProductUnit> existingProductUnit = productUnitRepository.findByProductIdAndUnitId(productId,
                    newUnit.getId());
            if (existingProductUnit.isPresent() && !existingProductUnit.get().getId().equals(unitId)
                    && !existingProductUnit.get().getIsDeleted()) {
                throw new ProductException("Đơn vị tính '" + request.unitName() + "' đã tồn tại cho sản phẩm này");
            }

            productUnit.setUnit(newUnit);
        }

        // Cập nhật tỷ lệ quy đổi
        if (request.conversionValue() != null) {
            productUnit.setConversionValue(request.conversionValue());
        }

        // Cập nhật đơn vị cơ bản
        if (request.isBaseUnit() != null) {
            if (request.isBaseUnit()) {
                // Kiểm tra không có đơn vị cơ bản khác
                boolean hasOtherBaseUnit = productUnitRepository.findByProductIdAndIsBaseUnit(productId, true)
                        .filter(pu -> !pu.getId().equals(unitId))
                        .isPresent();
                if (hasOtherBaseUnit) {
                    throw new ProductException(
                            "Sản phẩm đã có đơn vị cơ bản khác. Chỉ được phép có một đơn vị cơ bản duy nhất");
                }

                // Tự động điều chỉnh conversionValue = 1 cho đơn vị cơ bản
                if (request.conversionValue() != null && request.conversionValue() != 1) {
                    log.info("Tự động điều chỉnh conversionValue từ {} thành 1 cho đơn vị cơ bản",
                            request.conversionValue());
                }
                if (productUnit.getConversionValue() != 1) {
                    productUnit.setConversionValue(1);
                }
            }
            productUnit.setIsBaseUnit(request.isBaseUnit());
        }

        // Cập nhật mã code
        if (request.code() != null) {
            if (productUnitRepository.existsByCodeAndIdNot(request.code(), unitId)) {
                throw new ProductException("Mã đơn vị sản phẩm '" + request.code() + "' đã tồn tại");
            }
            productUnit.setCode(request.code());
        }

        // Cập nhật barcode
        if (request.barcode() != null) {
            if (productUnitRepository.existsByBarcodeAndIdNot(request.barcode(), unitId)) {
                throw new ProductException("Mã vạch '" + request.barcode() + "' đã tồn tại");
            }
            productUnit.setBarcode(request.barcode());
        }

        // Cập nhật trạng thái hoạt động
        if (request.isActive() != null) {
            productUnit.setIsActive(request.isActive());
        }

        ProductUnit updatedProductUnit = productUnitRepository.save(productUnit);
        log.info("Đã cập nhật đơn vị sản phẩm ID: {} của sản phẩm ID: {}", unitId, productId);

        return mapToProductUnitResponse(updatedProductUnit);
    }

    /**
     * Xóa đơn vị khỏi sản phẩm (soft delete)
     */
    @Override
    @Transactional
    public void deleteProductUnit(Long productId, Long unitId) {
        log.info("Bắt đầu xóa đơn vị sản phẩm ID: {} khỏi sản phẩm ID: {}", unitId, productId);

        // Tìm ProductUnit
        ProductUnit productUnit = productUnitRepository.findByIdAndProductId(unitId, productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Không tìm thấy đơn vị sản phẩm với ID: " + unitId + " thuộc sản phẩm ID: " + productId));

        // Không cho phép xóa đơn vị cơ bản nếu là đơn vị duy nhất
        if (productUnit.getIsBaseUnit()) {
            long totalUnits = productUnitRepository.countByProductId(productId);
            if (totalUnits <= 1) {
                throw new ProductException("Không thể xóa đơn vị cơ bản duy nhất của sản phẩm");
            }
        }

        // Soft delete
        productUnit.setIsDeleted(true);
        productUnit.setIsActive(false);
        productUnitRepository.save(productUnit);

        log.info("Đã xóa đơn vị sản phẩm ID: {} khỏi sản phẩm ID: {}", unitId, productId);
    }

    /**
     * Lấy danh sách đơn vị của sản phẩm
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductUnitResponse> getProductUnits(Long productId) {
        log.info("Lấy danh sách đơn vị của sản phẩm ID: {}", productId);

        // Kiểm tra sản phẩm tồn tại
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException("Không tìm thấy sản phẩm với ID: " + productId);
        }

        List<ProductUnit> productUnits = productUnitRepository.findActiveByProductId(productId);

        return productUnits.stream()
                .map(this::mapToProductUnitResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy thông tin đơn vị sản phẩm theo ID
     */
    @Override
    @Transactional(readOnly = true)
    public ProductUnitResponse getProductUnit(Long productId, Long unitId) {
        log.info("Lấy thông tin đơn vị sản phẩm ID: {} của sản phẩm ID: {}", unitId, productId);

        ProductUnit productUnit = productUnitRepository.findByIdAndProductId(unitId, productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Không tìm thấy đơn vị sản phẩm với ID: " + unitId + " thuộc sản phẩm ID: " + productId));

        return mapToProductUnitResponse(productUnit);
    }

    /**
     * Lấy đơn vị cơ bản của sản phẩm
     */
    @Override
    @Transactional(readOnly = true)
    public ProductUnitResponse getBaseProductUnit(Long productId) {
        log.info("Lấy đơn vị cơ bản của sản phẩm ID: {}", productId);

        // Kiểm tra sản phẩm tồn tại
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException("Không tìm thấy sản phẩm với ID: " + productId);
        }

        ProductUnit baseUnit = productUnitRepository.findByProductIdAndIsBaseUnit(productId, true)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Không tìm thấy đơn vị cơ bản cho sản phẩm ID: " + productId));

        return mapToProductUnitResponse(baseUnit);
    }

    /**
     * Chuyển đổi ProductUnit entity sang ProductUnitResponse DTO
     */
    private ProductUnitResponse mapToProductUnitResponse(ProductUnit productUnit) {
        // Tạo UnitDto từ Unit entity
        UnitDto unitDto = new UnitDto();
        unitDto.setId(productUnit.getUnit().getId());
        unitDto.setName(productUnit.getUnit().getName());
        unitDto.setIsActive(productUnit.getUnit().getIsActive());
        unitDto.setIsDeleted(productUnit.getUnit().getIsDeleted());
        unitDto.setCreatedAt(productUnit.getUnit().getCreatedAt());
        unitDto.setUpdatedAt(productUnit.getUnit().getUpdatedAt());

        return new ProductUnitResponse(
                productUnit.getId(),
                productUnit.getCode(),
                productUnit.getBarcode(),
                productUnit.getConversionValue(),
                productUnit.getIsBaseUnit(),
                productUnit.getIsActive(),
                unitDto,
                productUnit.getProduct().getId(),
                productUnit.getCreatedAt(),
                productUnit.getUpdatedAt());
    }

    /**
     * Chuyển đổi ProductUnit entity sang ProductUnitSummary DTO
     */
    private ProductListResponse.ProductUnitSummary mapToProductUnitSummary(ProductUnit productUnit) {
        ProductListResponse.ProductUnitSummary summary = new ProductListResponse.ProductUnitSummary();
        summary.setId(productUnit.getId());
        summary.setCode(productUnit.getCode());
        summary.setBarcode(productUnit.getBarcode());
        summary.setConversionValue(productUnit.getConversionValue());
        summary.setIsBaseUnit(productUnit.getIsBaseUnit());
        summary.setIsActive(productUnit.getIsActive());

        // Set thông tin đơn vị tính
        if (productUnit.getUnit() != null) {
            summary.setUnitName(productUnit.getUnit().getName());
            summary.setUnitId(productUnit.getUnit().getId());
        }

        return summary;
    }
}
