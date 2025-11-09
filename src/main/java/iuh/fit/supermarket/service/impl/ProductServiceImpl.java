package iuh.fit.supermarket.service.impl;

import iuh.fit.supermarket.dto.product.*;
import iuh.fit.supermarket.dto.unit.UnitDto;
import iuh.fit.supermarket.entity.Brand;
import iuh.fit.supermarket.entity.Category;
import iuh.fit.supermarket.entity.Product;
import iuh.fit.supermarket.entity.ProductUnit;
import iuh.fit.supermarket.entity.ProductUnitImage;
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
    private final iuh.fit.supermarket.repository.WarehouseRepository warehouseRepository;
    private final iuh.fit.supermarket.service.PriceService priceService;
    private final iuh.fit.supermarket.repository.ProductUnitImageRepository productUnitImageRepository;

    /**
     * Tạo sản phẩm mới
     */
    @Override
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("Bắt đầu tạo sản phẩm mới: {}", request.getName());

        // Sinh mã sản phẩm nếu không được cung cấp
        String productCode = request.getCode();
        if (productCode == null || productCode.trim().isEmpty()) {
            productCode = generateProductCode();
            log.debug("Tự động sinh mã sản phẩm: {}", productCode);
        } else {
            productCode = productCode.trim();
        }

        // Kiểm tra trùng lặp mã sản phẩm
        if (productRepository.existsByCode(productCode)) {
            throw DuplicateProductException.forProductCode(productCode);
        }

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
        product.setCode(productCode);
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

        // Kiểm tra trùng lặp mã sản phẩm (nếu có thay đổi code)
        if (request.getCode() != null && !request.getCode().trim().isEmpty()) {
            String newCode = request.getCode().trim();
            if (!newCode.equals(product.getCode())) {
                if (productRepository.existsByCodeAndIdNot(newCode, id)) {
                    throw new ProductException("Mã sản phẩm đã tồn tại: " + newCode);
                }
                product.setCode(newCode);
            }
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

        // Sử dụng query với đầy đủ các filter
        String searchTermClean = (searchTerm != null && !searchTerm.trim().isEmpty()) ? searchTerm.trim() : "";
        Page<Product> productPage = productRepository.findProductsAdvanced(
                searchTermClean,
                categoryId,
                brandId,
                isActive,
                isRewardPoint,
                pageable);

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
        response.setCode(product.getCode());
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
        summary.setProductCode(product.getCode());
        summary.setName(product.getName());
        summary.setDescription(product.getDescription());
        summary.setIsActive(product.getIsActive());
        summary.setIsRewardPoint(product.getIsRewardPoint());
        summary.setCreatedDate(product.getCreatedAt());
        summary.setUpdatedAt(product.getUpdatedAt());

        // Set thông tin thương hiệu và danh mục
        summary.setBrandId(product.getBrand() != null ? product.getBrand().getBrandId() : null);
        summary.setBrandName(product.getBrand() != null ? product.getBrand().getName() : null);
        summary.setCategoryId(product.getCategory() != null ? product.getCategory().getCategoryId() : null);
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

        // Kiểm tra barcode không trùng lặp nếu có
        for (ProductUnitRequest unitRequest : units) {
            if (unitRequest.barcode() != null && !unitRequest.barcode().trim().isEmpty()) {
                if (productUnitRepository.existsByBarcode(unitRequest.barcode().trim())) {
                    throw new ProductException("Mã vạch '" + unitRequest.barcode() + "' đã tồn tại trong hệ thống");
                }
            }
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

            productUnit.setConversionValue(unitRequest.conversionValue());
            productUnit.setIsBaseUnit(unitRequest.isBaseUnit());
            productUnit.setBarcode(unitRequest.barcode());
            productUnit.setIsActive(true);
            productUnit.setIsDeleted(false);

            productUnitRepository.save(productUnit);
            log.debug("Đã tạo đơn vị sản phẩm cho sản phẩm ID: {}", product.getId());
        }
    }

    /**
     * Sinh mã sản phẩm tự động duy nhất theo format SPxxxxx
     * Ví dụ: SP00001, SP00002, ..., SP99999
     */
    private String generateProductCode() {
        // Lấy tất cả mã sản phẩm hiện tại bắt đầu với "SP" và extract số
        long nextNumber = 1;
        
        // Tìm mã sản phẩm có số lớn nhất
        List<Product> products = productRepository.findAll();
        for (Product product : products) {
            if (product.getCode() != null && product.getCode().startsWith("SP")) {
                try {
                    String numberPart = product.getCode().substring(2);
                    long number = Long.parseLong(numberPart);
                    if (number >= nextNumber) {
                        nextNumber = number + 1;
                    }
                } catch (NumberFormatException e) {
                    log.debug("Không thể parse mã sản phẩm: {}", product.getCode());
                }
            }
        }

        // Kiểm tra không vượt quá giới hạn (SP99999)
        if (nextNumber > 99999) {
            throw new ProductException("Số lượng mã sản phẩm đã vượt quá giới hạn (99999)");
        }

        String code = String.format("SP%05d", nextNumber);
        
        // Double-check để chắc chắn mã không trùng
        if (productRepository.existsByCode(code)) {
            throw new ProductException("Mã sản phẩm " + code + " đã tồn tại");
        }

        return code;
    }

    /**
     * Chuyển đổi ProductUnit entity sang ProductUnitInfo DTO
     */
    private ProductResponse.ProductUnitInfo mapToProductUnitInfo(ProductUnit productUnit) {
        ProductResponse.ProductUnitInfo info = new ProductResponse.ProductUnitInfo();
        info.setId(productUnit.getId());
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

        productUnit.setConversionValue(request.conversionValue());
        productUnit.setIsBaseUnit(request.isBaseUnit());
        productUnit.setBarcode(request.barcode());
        productUnit.setIsActive(true);
        productUnit.setIsDeleted(false);

        ProductUnit savedProductUnit = productUnitRepository.save(productUnit);
        log.info("Đã thêm đơn vị sản phẩm cho sản phẩm ID: {}", productId);

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
     * Tìm kiếm ProductUnit theo tên sản phẩm, mã code hoặc barcode
     */
    @Override
    @Transactional(readOnly = true)
    public List<ProductUnitResponse> searchProductUnits(String searchTerm) {
        log.info("Tìm kiếm ProductUnit với từ khóa: {}", searchTerm);

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            log.warn("Từ khóa tìm kiếm rỗng");
            return List.of();
        }

        List<ProductUnit> productUnits = productUnitRepository.searchProductUnits(searchTerm.trim());
        log.info("Tìm thấy {} ProductUnit", productUnits.size());

        return productUnits.stream()
                .map(this::mapToProductUnitResponse)
                .collect(Collectors.toList());
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

        // Map danh sách hình ảnh của ProductUnit
        List<ProductUnitImageDto> images = List.of();
        if (productUnit.getProductUnitImages() != null && !productUnit.getProductUnitImages().isEmpty()) {
            images = productUnit.getProductUnitImages().stream()
                    .filter(pui -> pui.getIsActive() != null && pui.getIsActive())
                    .sorted((a, b) -> {
                        // Sắp xếp: primary trước, sau đó theo displayOrder
                        if (a.getIsPrimary() && !b.getIsPrimary())
                            return -1;
                        if (!a.getIsPrimary() && b.getIsPrimary())
                            return 1;
                        return Integer.compare(
                                a.getDisplayOrder() != null ? a.getDisplayOrder() : 0,
                                b.getDisplayOrder() != null ? b.getDisplayOrder() : 0);
                    })
                    .map(this::mapToProductUnitImageDto)
                    .collect(Collectors.toList());
        }

        return new ProductUnitResponse(
                productUnit.getId(),
                productUnit.getBarcode(),
                productUnit.getConversionValue(),
                productUnit.getIsBaseUnit(),
                productUnit.getIsActive(),
                unitDto,
                productUnit.getProduct().getId(),
                images,
                productUnit.getCreatedAt(),
                productUnit.getUpdatedAt());
    }

    /**
     * Chuyển đổi ProductUnitImage entity sang ProductUnitImageDto
     */
    private ProductUnitImageDto mapToProductUnitImageDto(ProductUnitImage productUnitImage) {
        ProductUnitImageDto dto = new ProductUnitImageDto();
        dto.setId(productUnitImage.getId());
        dto.setDisplayOrder(productUnitImage.getDisplayOrder());
        dto.setIsPrimary(productUnitImage.getIsPrimary());
        dto.setIsActive(productUnitImage.getIsActive());
        dto.setCreatedAt(productUnitImage.getCreatedAt());
        dto.setProductUnitId(productUnitImage.getProductUnit().getId());

        // Map ProductImage
        if (productUnitImage.getProductImage() != null) {
            ProductImageDto imageDto = new ProductImageDto();
            imageDto.setImageId(productUnitImage.getProductImage().getImageId());
            imageDto.setImageUrl(productUnitImage.getProductImage().getImageUrl());
            imageDto.setImageAlt(productUnitImage.getProductImage().getImageAlt());
            imageDto.setSortOrder(productUnitImage.getProductImage().getSortOrder());
            imageDto.setCreatedAt(productUnitImage.getProductImage().getCreatedAt());
            imageDto.setProductId(productUnitImage.getProductImage().getProduct().getId());

            dto.setProductImage(imageDto);
        }

        return dto;
    }

    /**
     * Chuyển đổi ProductUnit entity sang ProductUnitSummary DTO
     */
    private ProductListResponse.ProductUnitSummary mapToProductUnitSummary(ProductUnit productUnit) {
        ProductListResponse.ProductUnitSummary summary = new ProductListResponse.ProductUnitSummary();
        summary.setId(productUnit.getId());
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

    /**
     * Lấy thông tin chi tiết đầy đủ của ProductUnit
     * Bao gồm tên sản phẩm, tên đơn vị, số lượng tồn kho và giá hiện tại
     */
    @Override
    @Transactional(readOnly = true)
    public ProductUnitDetailResponse getProductUnitDetails(Long productUnitId) {
        log.info("Lấy thông tin chi tiết đầy đủ của ProductUnit ID: {}", productUnitId);

        // Tìm ProductUnit
        ProductUnit productUnit = productUnitRepository.findById(productUnitId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Không tìm thấy đơn vị sản phẩm với ID: " + productUnitId));

        // Lấy thông tin sản phẩm
        Product product = productUnit.getProduct();
        if (product == null) {
            throw new ProductException("ProductUnit không có thông tin sản phẩm");
        }

        // Lấy thông tin đơn vị tính
        Unit unit = productUnit.getUnit();
        if (unit == null) {
            throw new ProductException("ProductUnit không có thông tin đơn vị tính");
        }

        // Build response cơ bản
        ProductUnitDetailResponse.ProductUnitDetailResponseBuilder builder = ProductUnitDetailResponse.builder()
                .productUnitId(productUnit.getId())
                .barcode(productUnit.getBarcode())
                .conversionValue(productUnit.getConversionValue())
                .isBaseUnit(productUnit.getIsBaseUnit())
                .isActive(productUnit.getIsActive())
                .productId(product.getId())
                .productName(product.getName())
                .productCode(product.getCode())
                .unitId(unit.getId())
                .unitName(unit.getName());

        // Lấy số lượng tồn kho
        try {
            Optional<iuh.fit.supermarket.entity.Warehouse> warehouseOpt = warehouseRepository
                    .findByProductUnitId(productUnitId);
            if (warehouseOpt.isPresent()) {
                builder.quantityOnHand(warehouseOpt.get().getQuantityOnHand());
                log.info("Tồn kho của ProductUnit ID {}: {}", productUnitId, warehouseOpt.get().getQuantityOnHand());
            } else {
                builder.quantityOnHand(0);
                log.info("Không tìm thấy thông tin tồn kho cho ProductUnit ID: {}, đặt mặc định là 0", productUnitId);
            }
        } catch (Exception e) {
            log.warn("Lỗi khi lấy thông tin tồn kho cho ProductUnit ID {}: {}", productUnitId, e.getMessage());
            builder.quantityOnHand(0);
        }

        // Lấy giá hiện tại từ bảng giá đang áp dụng
        try {
            iuh.fit.supermarket.dto.price.PriceDetailDto currentPrice = priceService
                    .getCurrentPriceByProductUnitId(productUnitId);
            if (currentPrice != null) {
                builder.currentPrice(currentPrice.getSalePrice());
                log.info("Giá hiện tại của ProductUnit ID {}: {}", productUnitId, currentPrice.getSalePrice());

                // Lấy thông tin bảng giá nếu có
                if (currentPrice.getPriceDetailId() != null) {
                    try {
                        // Lấy thông tin bảng giá từ PriceDetail
                        // Note: Cần thêm logic để lấy thông tin Price từ PriceDetail
                        log.debug("ProductUnit ID {} có giá trong bảng giá ID: {}", productUnitId,
                                currentPrice.getPriceDetailId());
                    } catch (Exception ex) {
                        log.warn("Không thể lấy thông tin bảng giá: {}", ex.getMessage());
                    }
                }
            } else {
                log.info("Không tìm thấy giá hiện tại cho ProductUnit ID: {}", productUnitId);
            }
        } catch (Exception e) {
            log.warn("Lỗi khi lấy giá hiện tại cho ProductUnit ID {}: {}", productUnitId, e.getMessage());
        }

        // Lấy danh sách hình ảnh của ProductUnit
        try {
            List<ProductUnitImage> productUnitImages = productUnitImageRepository
                    .findByProductUnitIdOrderByDisplayOrder(productUnitId);

            if (!productUnitImages.isEmpty()) {
                List<ProductUnitImageDto> imageDtos = productUnitImages.stream()
                        .map(this::mapToProductUnitImageDto)
                        .collect(Collectors.toList());
                builder.images(imageDtos);
                log.info("Đã load {} hình ảnh cho ProductUnit ID: {}", imageDtos.size(), productUnitId);
            } else {
                log.info("ProductUnit ID {} không có hình ảnh", productUnitId);
            }
        } catch (Exception e) {
            log.warn("Lỗi khi lấy hình ảnh cho ProductUnit ID {}: {}", productUnitId, e.getMessage());
        }

        ProductUnitDetailResponse response = builder.build();
        log.info("Đã lấy thông tin chi tiết đầy đủ của ProductUnit ID: {}", productUnitId);

        return response;
    }

    // ==================== METHODS CHO AI CHAT ====================

    /**
     * Tìm kiếm sản phẩm cho AI chat
     * Trả về kết quả dạng text format phù hợp cho AI response
     */
    @Override
    @Transactional(readOnly = true)
    public String searchProductsForAI(String query, int limit) {
        log.info("🤖 AI Tool: searchProductsForAI với query='{}', limit={}", query, limit);

        try {
            // Tìm kiếm sản phẩm theo tên hoặc mã
            // TODO: Implement searchByNameOrCode trong ProductUnitRepository
            List<ProductUnit> productUnits = productUnitRepository
                    .findAll()  // Tạm thời dùng findAll, cần implement search method
                    .stream()
                    .filter(unit -> {
                        String productName = unit.getProduct().getName().toLowerCase();
                        String barcode = unit.getBarcode() != null ? unit.getBarcode().toLowerCase() : "";
                        String searchTerm = query.toLowerCase();
                        return productName.contains(searchTerm) || barcode.contains(searchTerm);
                    })
                    .limit(limit)
                    .collect(Collectors.toList());

            if (productUnits.isEmpty()) {
                return "Không tìm thấy sản phẩm nào với từ khóa: " + query;
            }

            // Format kết quả cho AI dễ đọc với JSON-like format để AI dễ parse
            StringBuilder result = new StringBuilder();
            int validProductCount = 0; // Đếm số sản phẩm có giá

            for (ProductUnit unit : productUnits) {
                Product product = unit.getProduct();

                // Lấy giá hiện tại
                String price = null;
                try {
                    var priceInfo = priceService.getCurrentPriceByProductUnitId(unit.getId());
                    if (priceInfo != null && priceInfo.getSalePrice() != null) {
                        price = priceInfo.getSalePrice().toString();
                    }
                } catch (Exception e) {
                    log.debug("Không lấy được giá cho ProductUnit ID: {}", unit.getId());
                }

                // Skip sản phẩm không có giá hoặc giá = 0
                if (price == null || price.equals("0") || price.equals("0.0")) {
                    log.debug("Bỏ qua ProductUnit ID: {} vì không có giá", unit.getId());
                    continue;
                }

                // Lấy tồn kho
                String stockStatus = "Hết hàng";
                int quantity = 0;
                try {
                    var warehouse = warehouseRepository.findByProductUnitId(unit.getId());
                    if (warehouse.isPresent()) {
                        quantity = warehouse.get().getQuantityOnHand();
                        stockStatus = quantity > 0 ? "Còn hàng" : "Hết hàng";
                    }
                } catch (Exception e) {
                    log.debug("Không lấy được tồn kho cho ProductUnit ID: {}", unit.getId());
                }

                // Lấy main image URL
                String imageUrl = null;
                try {
                    Optional<ProductUnitImage> primaryImage =
                        productUnitImageRepository.findPrimaryImageByProductUnitId(unit.getId());
                    if (primaryImage.isPresent()) {
                        imageUrl = primaryImage.get().getProductImage().getImageUrl();
                    }
                } catch (Exception e) {
                    log.debug("Không lấy được image cho ProductUnit ID: {}", unit.getId());
                }

                // Format dạng JSON-like để AI dễ parse
                result.append(String.format("""

                [PRODUCT]
                product_unit_id: %d
                name: %s (%s)
                code: %s
                price: %s
                unit: %s
                brand: %s
                stock_status: %s
                stock_quantity: %d
                image_url: %s
                description: %s
                [/PRODUCT]
                """,
                        unit.getId(),
                        product.getName(),
                        unit.getUnit().getName(),
                        unit.getBarcode() != null ? unit.getBarcode() : "N/A",
                        price,
                        unit.getUnit().getName(),
                        product.getBrand() != null ? product.getBrand().getName() : "N/A",
                        stockStatus,
                        quantity,
                        imageUrl != null ? imageUrl : "N/A",
                        String.format("%s %s", product.getName(), unit.getUnit().getName())
                ));

                validProductCount++;
            }

            // Kiểm tra nếu không có sản phẩm nào có giá
            if (validProductCount == 0) {
                return "Tìm thấy sản phẩm nhưng hiện không có giá bán. Vui lòng liên hệ CSKH để biết thêm chi tiết.";
            }

            // Thêm header với số lượng sản phẩm thực sự có giá
            return String.format("Tìm thấy %d sản phẩm:\n", validProductCount) + result.toString();
        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm sản phẩm cho AI", e);
            return "Xin lỗi, có lỗi khi tìm kiếm sản phẩm. Vui lòng thử lại.";
        }
    }

    /**
     * Kiểm tra tồn kho cho AI chat
     * Trả về tình trạng tồn kho dạng text
     */
    @Override
    @Transactional(readOnly = true)
    public String checkStockForAI(Long productId) {
        log.info("🤖 AI Tool: checkStockForAI cho productId={}", productId);

        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));

            StringBuilder result = new StringBuilder();
            result.append(String.format("📦 Tồn kho sản phẩm: %s\n", product.getName()));

            // Lấy tất cả đơn vị của sản phẩm
            List<ProductUnit> units = productUnitRepository.findByProductId(productId);

            for (ProductUnit unit : units) {
                var warehouse = warehouseRepository.findByProductUnitId(unit.getId());

                String stockInfo = "Không có thông tin";
                if (warehouse.isPresent()) {
                    int quantity = warehouse.get().getQuantityOnHand();
                    if (quantity > 10) {
                        stockInfo = String.format("✅ Còn hàng: %d %s", quantity, unit.getUnit().getName());
                    } else if (quantity > 0) {
                        stockInfo = String.format("⚠️ Sắp hết: %d %s", quantity, unit.getUnit().getName());
                    } else {
                        stockInfo = "❌ Hết hàng";
                    }
                }

                result.append(String.format("\n- %s: %s",
                        unit.getUnit().getName(),
                        stockInfo));
            }

            return result.toString();
        } catch (ProductNotFoundException e) {
            return "Không tìm thấy sản phẩm với ID: " + productId;
        } catch (Exception e) {
            log.error("Lỗi khi kiểm tra tồn kho cho AI", e);
            return "Xin lỗi, không thể kiểm tra tồn kho lúc này.";
        }
    }

    /**
     * Lấy chi tiết sản phẩm cho AI chat
     * Trả về thông tin chi tiết dạng text format
     */
    @Override
    @Transactional(readOnly = true)
    public String getProductDetailsForAI(Long productId) {
        log.info("🤖 AI Tool: getProductDetailsForAI cho productId={}", productId);

        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));

            StringBuilder result = new StringBuilder();
            result.append(String.format("📋 THÔNG TIN CHI TIẾT SẢN PHẨM\n\n"));
            result.append(String.format("🏷️ Tên: %s\n", product.getName()));

            if (product.getDescription() != null && !product.getDescription().isEmpty()) {
                result.append(String.format("📝 Mô tả: %s\n", product.getDescription()));
            }

            result.append(String.format("📂 Danh mục: %s\n", product.getCategory().getName()));
            result.append(String.format("🏭 Thương hiệu: %s\n", product.getBrand().getName()));

            // Thông tin đơn vị và giá
            result.append("\n💰 BẢNG GIÁ:\n");
            List<ProductUnit> units = productUnitRepository.findByProductId(productId);

            for (ProductUnit unit : units) {
                String price = "Liên hệ";
                try {
                    var priceInfo = priceService.getCurrentPriceByProductUnitId(unit.getId());
                    if (priceInfo != null && priceInfo.getSalePrice() != null) {
                        price = String.format("%,.0fđ", priceInfo.getSalePrice());
                    }
                } catch (Exception e) {
                    log.debug("Không lấy được giá cho ProductUnit ID: {}", unit.getId());
                }

                result.append(String.format("- %s: %s",
                        unit.getUnit().getName(),
                        price));

                if (!unit.getIsBaseUnit() && unit.getConversionValue() != null) {
                    result.append(String.format(" (Quy đổi 1:%d)", unit.getConversionValue()));
                }
                result.append("\n");
            }

            // Thông tin khác
            if (product.getIsRewardPoint()) {
                result.append("\n✨ Sản phẩm được tích điểm thưởng");
            }

            if (!product.getIsActive()) {
                result.append("\n⚠️ Sản phẩm tạm ngưng kinh doanh");
            }

            return result.toString();
        } catch (ProductNotFoundException e) {
            return "Không tìm thấy sản phẩm với ID: " + productId;
        } catch (Exception e) {
            log.error("Lỗi khi lấy chi tiết sản phẩm cho AI", e);
            return "Xin lỗi, không thể lấy thông tin chi tiết sản phẩm lúc này.";
        }
    }
}
